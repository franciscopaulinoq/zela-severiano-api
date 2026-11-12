"""Revisa um PR com os modelos configurados, em cada nível de instrução pedido.

Uso: python review_pr.py --pr 30 --experimento E1 --niveis-prompt P1,P2,P3

As revisões rodam em paralelo. Só erros técnicos (timeout, limite de requisições, 5xx,
conexão) geram nova tentativa. Toda revisão, inclusive as que falham, é gravada em CSV.
"""

from __future__ import annotations

import argparse
import asyncio
import sys
import time
import uuid
from datetime import datetime, timezone

from tenacity import AsyncRetrying, retry_if_exception, stop_after_attempt, wait_exponential

from clients.anthropic_client import ClienteAnthropic
from clients.base import ClienteModelo
from clients.gemini_client import ClienteGemini
from clients.openai_client import ClienteOpenAI
from config import Configuracao, carregar_configuracao
from context_builder import montar_contexto
from github_client import ClienteGitHub
from models import ContextoPR, Revisao
from parser_resposta import extrair_comentarios
from prompt_builder import NIVEIS_PROMPT, montar_prompt, sha256
from storage import salvar_revisao


def montar_clientes(config: Configuracao) -> list[ClienteModelo]:
    parametros = (config.esforco_raciocinio, config.max_tokens_saida)
    clientes: list[ClienteModelo] = []
    if config.chave_openai:
        clientes.append(ClienteOpenAI(config.chave_openai, config.modelo_openai, *parametros))
    if config.chave_anthropic:
        clientes.append(ClienteAnthropic(config.chave_anthropic, config.modelo_anthropic, *parametros))
    if config.chave_gemini:
        clientes.append(ClienteGemini(config.chave_gemini, config.modelo_gemini, *parametros))
    if not clientes:
        raise RuntimeError("Nenhuma chave de API configurada (OPENAI_API_KEY, ANTHROPIC_API_KEY ou GEMINI_API_KEY).")
    return clientes


async def revisar(
    cliente: ClienteModelo,
    prompt: str,
    nivel: str,
    contexto: ContextoPR,
    config: Configuracao,
    run_id: str,
    experimento: str,
) -> Revisao:
    revisao = Revisao(
        run_id=run_id,
        experimento=experimento,
        modelo=cliente.modelo,
        nivel_prompt=nivel,
        prompt_sha256=sha256(prompt),
        pr_numero=contexto.numero_pr,
        issue_numero=contexto.numero_issue,
        effort=config.esforco_raciocinio,
    )
    inicio = time.monotonic()
    try:
        async for tentativa in AsyncRetrying(
            stop=stop_after_attempt(config.max_tentativas),
            wait=wait_exponential(multiplier=2, min=2, max=60),
            retry=retry_if_exception(_erro_tecnico),
            reraise=True,
        ):
            with tentativa:
                resposta = await cliente.gerar(prompt, config.timeout_segundos)
    except Exception as erro:
        revisao.status = _status_do_erro(erro)
        revisao.erro_detalhe = f"{type(erro).__name__}: {erro}"
    else:
        revisao.comentario_gerado = resposta.texto
        revisao.modelo_retornado = resposta.modelo_retornado
        revisao.motivo_parada = resposta.motivo_parada
        revisao.tokens_entrada = resposta.tokens_entrada
        revisao.tokens_saida = resposta.tokens_saida
        revisao.tokens_raciocinio = resposta.tokens_raciocinio
        revisao.json_valido, revisao.comentarios = extrair_comentarios(resposta.texto)
    revisao.latencia_ms = int((time.monotonic() - inicio) * 1000)
    return revisao


async def revisar_pr(numero_pr: int, experimento: str, niveis: list[str], run_id: str) -> list[Revisao]:
    config = carregar_configuracao()
    clientes = montar_clientes(config)
    contexto = montar_contexto(config, ClienteGitHub(config), numero_pr)

    tarefas = []
    for nivel in niveis:
        prompt = montar_prompt(contexto, nivel)
        tarefas += [revisar(cliente, prompt, nivel, contexto, config, run_id, experimento) for cliente in clientes]
    revisoes = await asyncio.gather(*tarefas)

    for revisao in revisoes:
        salvar_revisao(config.csv_revisoes, config.csv_comentarios, revisao)
    return list(revisoes)


def _erro_tecnico(erro: BaseException) -> bool:
    if isinstance(erro, (asyncio.TimeoutError, TimeoutError, ConnectionError)):
        return True
    nome = type(erro).__name__.lower()
    if any(trecho in nome for trecho in ("timeout", "connection", "ratelimit", "internalserver", "servererror")):
        return True
    codigo = getattr(erro, "status_code", None) or getattr(erro, "code", None)
    return isinstance(codigo, int) and (codigo == 429 or codigo >= 500)


def _status_do_erro(erro: BaseException) -> str:
    nome = type(erro).__name__.lower()
    mensagem = str(erro).lower()
    if isinstance(erro, (asyncio.TimeoutError, TimeoutError)) or "timeout" in nome:
        return "timeout"
    if "ratelimit" in nome or "429" in mensagem or "quota" in mensagem:
        return "rate_limited"
    return "erro"


def _niveis(valor: str) -> list[str]:
    niveis = [nivel.strip().upper() for nivel in valor.split(",") if nivel.strip()]
    if not niveis or any(nivel not in NIVEIS_PROMPT for nivel in niveis):
        raise argparse.ArgumentTypeError(f"use valores entre {', '.join(NIVEIS_PROMPT)}")
    return niveis


def main() -> None:
    parser = argparse.ArgumentParser(description="Revisão de PR por modelos de IA.")
    parser.add_argument("--pr", type=int, required=True, help="número do pull request")
    parser.add_argument("--experimento", choices=["E1", "PILOTO"], required=True)
    parser.add_argument("--niveis-prompt", type=_niveis, default=["P2"])
    parser.add_argument(
        "--run-id",
        default=datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + uuid.uuid4().hex[:6],
        help="identificador da execução (o workflow passa o id do run do Actions)",
    )
    args = parser.parse_args()

    revisoes = asyncio.run(revisar_pr(args.pr, args.experimento, args.niveis_prompt, args.run_id))

    for revisao in revisoes:
        formato = {True: "json ok", False: "json inválido", None: "-"}[revisao.json_valido]
        print(
            f"[{revisao.status}] {revisao.modelo} {revisao.nivel_prompt} — {revisao.latencia_ms}ms, "
            f"{formato}, {len(revisao.comentarios)} comentário(s)"
        )

    if all(revisao.status != "ok" for revisao in revisoes):
        sys.exit(1)


if __name__ == "__main__":
    main()

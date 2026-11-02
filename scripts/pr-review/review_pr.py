"""CLI que executa a revisão automática de um PR pelos três modelos, em
todos os níveis de instrução e de completude da issue pedidos.

Uso:
    python review_pr.py --pr 30 --experimento E1 --niveis-prompt P1,P2,P3 --niveis-issue I3
    python review_pr.py --pr 30 --experimento E2 --niveis-prompt P2 --niveis-issue I1,I2

Todas as revisões do PR (modelos × níveis) rodam de forma assíncrona e
simultânea (asyncio.gather). Erros técnicos (timeout, limite de
requisições, 5xx, conexão) geram novas tentativas com espera exponencial.
Qualquer outro erro, e respostas fora do formato JSON, são registrados sem
nova tentativa. A falha de uma revisão nunca derruba as outras: tudo,
inclusive o erro, vai para o CSV. Nada é publicado no PR.
"""
from __future__ import annotations

import argparse
import asyncio
import sys
import time
import uuid
from datetime import datetime, timezone

from tenacity import AsyncRetrying, retry_if_exception, stop_after_attempt, wait_exponential

from clients.anthropic_client import AnthropicClient
from clients.base import ModelClient
from clients.gemini_client import GeminiClient
from clients.openai_client import OpenAIClient
from config import Settings, load_settings
from context_builder import montar_contexto
from github_client import GitHubClient
from models import ModelResponse, PRContext
from parser_resposta import extrair_comentarios
from prompt_builder import NIVEIS_ISSUE, NIVEIS_PROMPT, montar_prompt, sha256
from storage import salvar_resultado


def montar_clientes(settings: Settings) -> list[ModelClient]:
    parametros = (settings.reasoning_effort, settings.max_output_tokens)
    clientes: list[ModelClient] = []
    if settings.openai_api_key:
        clientes.append(OpenAIClient(settings.openai_api_key, settings.openai_model, *parametros))
    if settings.anthropic_api_key:
        clientes.append(AnthropicClient(settings.anthropic_api_key, settings.anthropic_model, *parametros))
    if settings.gemini_api_key:
        clientes.append(GeminiClient(settings.gemini_api_key, settings.gemini_model, *parametros))
    if not clientes:
        raise RuntimeError(
            "Nenhuma API key configurada (OPENAI_API_KEY / ANTHROPIC_API_KEY / GEMINI_API_KEY)."
        )
    return clientes


async def revisar_com_um_modelo(
    cliente: ModelClient,
    prompt: str,
    contexto: PRContext,
    settings: Settings,
    run_id: str,
    experimento: str,
    nivel_prompt: str,
    nivel_issue: str,
) -> ModelResponse:
    resposta = ModelResponse(
        run_id=run_id,
        experimento=experimento,
        modelo=cliente.nome,
        nivel_prompt=nivel_prompt,
        nivel_issue=nivel_issue,
        prompt_sha256=sha256(prompt),
        pr_numero=contexto.numero_pr,
        issue_numero=contexto.numero_issue,
        effort=settings.reasoning_effort,
        comentario_gerado="",
        status="ok",
        latencia_ms=0,
    )
    inicio = time.monotonic()
    try:
        async for tentativa in AsyncRetrying(
            stop=stop_after_attempt(settings.max_retries),
            wait=wait_exponential(multiplier=2, min=2, max=60),
            retry=retry_if_exception(_eh_erro_tecnico),
            reraise=True,
        ):
            with tentativa:
                bruta = await cliente.gerar(prompt, settings.request_timeout_seconds)
    except Exception as exc:  # nunca deixar uma exceção derrubar as outras revisões
        resposta.status = _classificar_erro(exc)
        resposta.erro_detalhe = f"{type(exc).__name__}: {exc}"
    else:
        resposta.comentario_gerado = bruta.texto
        resposta.modelo_retornado = bruta.modelo_retornado
        resposta.motivo_parada = bruta.motivo_parada
        resposta.tokens_entrada = bruta.tokens_entrada
        resposta.tokens_saida = bruta.tokens_saida
        resposta.tokens_raciocinio = bruta.tokens_raciocinio
        resposta.json_valido, resposta.comentarios = extrair_comentarios(bruta.texto)
    resposta.latencia_ms = int((time.monotonic() - inicio) * 1000)
    return resposta


def _eh_erro_tecnico(exc: BaseException) -> bool:
    """Só vale nova tentativa para o que não depende do conteúdo do prompt."""
    if isinstance(exc, (asyncio.TimeoutError, TimeoutError, ConnectionError)):
        return True
    nome = type(exc).__name__.lower()
    if any(trecho in nome for trecho in ("timeout", "connection", "ratelimit", "internalserver", "servererror")):
        return True
    codigo = getattr(exc, "status_code", None) or getattr(exc, "code", None)
    return isinstance(codigo, int) and (codigo == 429 or codigo >= 500)


def _classificar_erro(exc: BaseException) -> str:
    nome = type(exc).__name__.lower()
    texto = str(exc).lower()
    if isinstance(exc, (asyncio.TimeoutError, TimeoutError)) or "timeout" in nome:
        return "timeout"
    if "ratelimit" in nome or "429" in texto or "quota" in texto:
        return "rate_limited"
    return "erro"


async def revisar_pr(
    numero_pr: int, experimento: str, niveis_prompt: list[str], niveis_issue: list[str], run_id: str
) -> list[ModelResponse]:
    settings = load_settings()
    github = GitHubClient(settings)
    clientes = montar_clientes(settings)
    contexto = montar_contexto(settings, github, numero_pr)

    tarefas = []
    for nivel_prompt in niveis_prompt:
        for nivel_issue in niveis_issue:
            prompt = montar_prompt(contexto, nivel_prompt, nivel_issue)
            for cliente in clientes:
                tarefas.append(
                    revisar_com_um_modelo(
                        cliente, prompt, contexto, settings, run_id, experimento, nivel_prompt, nivel_issue
                    )
                )
    resultados = await asyncio.gather(*tarefas)

    for resultado in resultados:
        salvar_resultado(settings.results_csv_path, settings.comments_csv_path, resultado)
    return list(resultados)


def _lista_de_niveis(valor: str, permitidos: tuple[str, ...]) -> list[str]:
    niveis = [nivel.strip().upper() for nivel in valor.split(",") if nivel.strip()]
    invalidos = [nivel for nivel in niveis if nivel not in permitidos]
    if not niveis or invalidos:
        raise argparse.ArgumentTypeError(f"use valores entre {', '.join(permitidos)}")
    return niveis


def main() -> None:
    parser = argparse.ArgumentParser(description="Revisão automática de PR por 3 modelos de IA.")
    parser.add_argument("--pr", type=int, required=True, help="Número do Pull Request")
    parser.add_argument("--experimento", choices=["E1", "E2", "PILOTO"], required=True)
    parser.add_argument(
        "--niveis-prompt", type=lambda v: _lista_de_niveis(v, NIVEIS_PROMPT), default=["P2"]
    )
    parser.add_argument(
        "--niveis-issue", type=lambda v: _lista_de_niveis(v, NIVEIS_ISSUE), default=["I3"]
    )
    parser.add_argument(
        "--run-id",
        default=datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + uuid.uuid4().hex[:6],
        help="Identificador da execução (o workflow passa o id do run do Actions)",
    )
    args = parser.parse_args()

    resultados = asyncio.run(
        revisar_pr(args.pr, args.experimento, args.niveis_prompt, args.niveis_issue, args.run_id)
    )

    for r in resultados:
        formato = {True: "json ok", False: "json inválido", None: "-"}[r.json_valido]
        print(
            f"[{r.status}] {r.modelo} {r.nivel_prompt}/{r.nivel_issue} — {r.latencia_ms}ms, "
            f"{formato}, {len(r.comentarios)} comentário(s)"
        )

    if all(r.status != "ok" for r in resultados):
        # todas as revisões falharam: sinaliza erro para o step do Actions perceber
        sys.exit(1)


if __name__ == "__main__":
    main()

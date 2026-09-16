"""CLI que orquestra a revisão automática de um PR pelos três modelos.

Uso:
    python review_pr.py --pr 3
    python review_pr.py --pr 3 --post-comments

As chamadas às três APIs são feitas de forma assíncrona e simultânea
(asyncio.gather) — é o que o resumo do TCC chama de "chamadas assíncronas
... para os modelos GPT (OpenAI), Claude (Anthropic) e Gemini Pro (Google)"
usando o mesmo prompt e contexto. Erros de timeout/rate limit em um modelo
não derrubam os outros dois: cada chamada é isolada e o resultado (inclusive
o erro) é sempre registrado em CSV.
"""
from __future__ import annotations

import argparse
import asyncio
import sys
import time

from tenacity import AsyncRetrying, RetryError, stop_after_attempt, wait_exponential

from clients.anthropic_client import AnthropicClient
from clients.base import ModelClient
from clients.gemini_client import GeminiClient
from clients.openai_client import OpenAIClient
from config import Settings, load_settings
from context_builder import montar_contexto
from github_client import GitHubClient
from models import ModelResponse, PRContext
from prompt_builder import montar_prompt
from storage import salvar_resultado


def montar_clientes(settings: Settings) -> list[ModelClient]:
    clientes: list[ModelClient] = []
    if settings.openai_api_key:
        clientes.append(OpenAIClient(settings.openai_api_key, settings.openai_model))
    if settings.anthropic_api_key:
        clientes.append(AnthropicClient(settings.anthropic_api_key, settings.anthropic_model))
    if settings.gemini_api_key:
        clientes.append(GeminiClient(settings.gemini_api_key, settings.gemini_model))
    if not clientes:
        raise RuntimeError(
            "Nenhuma API key configurada (OPENAI_API_KEY / ANTHROPIC_API_KEY / GEMINI_API_KEY)."
        )
    return clientes


async def revisar_com_um_modelo(
    cliente: ModelClient, prompt: str, contexto: PRContext, settings: Settings
) -> ModelResponse:
    inicio = time.monotonic()
    try:
        async for tentativa in AsyncRetrying(
            stop=stop_after_attempt(settings.max_retries),
            wait=wait_exponential(multiplier=2, min=2, max=60),
            reraise=True,
        ):
            with tentativa:
                texto = await cliente.gerar(prompt, settings.request_timeout_seconds)
        status = "ok"
        erro_detalhe = None
    except RetryError as exc:
        texto = ""
        status = _classificar_erro(exc.last_attempt.exception())
        erro_detalhe = str(exc.last_attempt.exception())
    except Exception as exc:  # defesa extra: nunca deixar uma exceção subir e derrubar os outros modelos
        texto = ""
        status = _classificar_erro(exc)
        erro_detalhe = str(exc)

    latencia_ms = int((time.monotonic() - inicio) * 1000)
    return ModelResponse(
        modelo=cliente.nome,
        pr_numero=contexto.numero_pr,
        issue_numero=contexto.numero_issue,
        comentario_gerado=texto,
        status=status,
        latencia_ms=latencia_ms,
        erro_detalhe=erro_detalhe,
    )


def _classificar_erro(exc: BaseException | None) -> str:
    if exc is None:
        return "erro"
    nome = type(exc).__name__.lower()
    texto = str(exc).lower()
    if "timeout" in nome or "timeout" in texto:
        return "timeout"
    if "rate" in texto or "429" in texto or "quota" in texto:
        return "rate_limited"
    return "erro"


async def revisar_pr(numero_pr: int, postar_comentarios: bool) -> list[ModelResponse]:
    settings = load_settings()
    github = GitHubClient(settings)
    clientes = montar_clientes(settings)

    contexto = montar_contexto(settings, github, numero_pr)
    prompt = montar_prompt(contexto)

    resultados = await asyncio.gather(
        *(revisar_com_um_modelo(cliente, prompt, contexto, settings) for cliente in clientes)
    )

    for resultado in resultados:
        salvar_resultado(settings.results_csv_path, resultado)
        if postar_comentarios and resultado.status == "ok" and resultado.comentario_gerado:
            corpo = f"🤖 **Revisão automática — {resultado.modelo}**\n\n{resultado.comentario_gerado}"
            github.post_comment(numero_pr, corpo)

    return list(resultados)


def main() -> None:
    parser = argparse.ArgumentParser(description="Revisão automática de PR por 3 modelos de IA.")
    parser.add_argument("--pr", type=int, required=True, help="Número do Pull Request")
    parser.add_argument(
        "--post-comments",
        action="store_true",
        help="Publica cada resposta como comentário no PR (além de salvar no CSV)",
    )
    args = parser.parse_args()

    resultados = asyncio.run(revisar_pr(args.pr, args.post_comments))

    falhas = [r for r in resultados if r.status != "ok"]
    for resultado in resultados:
        print(f"[{resultado.status}] {resultado.modelo} — {resultado.latencia_ms}ms")

    if falhas and len(falhas) == len(resultados):
        # todos os modelos falharam: sinaliza erro para o step do Actions perceber
        sys.exit(1)


if __name__ == "__main__":
    main()

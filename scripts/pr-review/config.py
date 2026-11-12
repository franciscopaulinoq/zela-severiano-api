"""Configuração do pipeline, lida de variáveis de ambiente (ou de um `.env` local)."""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

RAIZ_REPOSITORIO = Path(__file__).resolve().parents[2]


@dataclass(frozen=True)
class Configuracao:
    token_github: str
    repositorio: str

    chave_openai: str | None
    modelo_openai: str
    chave_anthropic: str | None
    modelo_anthropic: str
    chave_gemini: str | None
    modelo_gemini: str

    esforco_raciocinio: str
    max_tokens_saida: int
    timeout_segundos: float
    max_tentativas: int

    csv_revisoes: str
    csv_comentarios: str
    caminho_contexto: str


def carregar_configuracao() -> Configuracao:
    return Configuracao(
        token_github=_obrigatoria("GITHUB_TOKEN"),
        repositorio=_obrigatoria("GITHUB_REPOSITORY", "franciscopaulinoq/zela-severiano-api"),
        chave_openai=os.getenv("OPENAI_API_KEY"),
        modelo_openai=os.getenv("OPENAI_MODEL", "gpt-6.1-sol"),
        chave_anthropic=os.getenv("ANTHROPIC_API_KEY"),
        modelo_anthropic=os.getenv("ANTHROPIC_MODEL", "claude-sonnet-5-5"),
        chave_gemini=os.getenv("GEMINI_API_KEY"),
        modelo_gemini=os.getenv("GEMINI_MODEL", "gemini-3.8-flash"),
        esforco_raciocinio=os.getenv("PR_REVIEW_REASONING_EFFORT", "medium"),
        max_tokens_saida=int(os.getenv("PR_REVIEW_MAX_OUTPUT_TOKENS", "16000")),
        timeout_segundos=float(os.getenv("PR_REVIEW_TIMEOUT_SECONDS", "300")),
        max_tentativas=int(os.getenv("PR_REVIEW_MAX_RETRIES", "4")),
        csv_revisoes=os.getenv("PR_REVIEW_RESULTS_CSV", "results/pr_reviews.csv"),
        csv_comentarios=os.getenv("PR_REVIEW_COMMENTS_CSV", "results/comentarios.csv"),
        caminho_contexto=os.getenv(
            "PR_REVIEW_PROJECT_CONTEXT", str(RAIZ_REPOSITORIO / "context" / "project_context.md")
        ),
    )


def _obrigatoria(nome: str, padrao: str | None = None) -> str:
    valor = os.getenv(nome, padrao)
    if not valor:
        raise RuntimeError(f"Variável de ambiente obrigatória ausente: {nome}")
    return valor

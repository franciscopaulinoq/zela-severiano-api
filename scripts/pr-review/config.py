"""Configuração central do pipeline de revisão automática por IA.

Todas as chaves de API e o token do GitHub vêm de variáveis de ambiente
(nunca hardcoded), para poderem ser injetadas como Secrets no GitHub Actions
sem tocar em código versionado.
"""
from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

# build_project_context.py escreve em <raiz-do-repo>/context/project_context.md
# usando um caminho absoluto (Path(__file__).resolve().parents[2]), independente
# do diretorio de trabalho. O default aqui precisa apontar pro mesmo lugar de
# forma absoluta - um default relativo ("context/project_context.md") so
# funciona por acaso quando o cwd é a raiz do repo, e quebra silenciosamente
# quando o script roda com working-directory: scripts/pr-review (como no
# workflow do GitHub Actions), retornando a mensagem de fallback "nao
# encontrado" em vez do contexto de fato.
_REPO_ROOT = Path(__file__).resolve().parents[2]


@dataclass(frozen=True)
class Settings:
    github_token: str
    github_repo: str

    openai_api_key: str | None
    openai_model: str

    anthropic_api_key: str | None
    anthropic_model: str

    gemini_api_key: str | None
    gemini_model: str

    request_timeout_seconds: float
    max_retries: int

    results_csv_path: str
    project_context_path: str


def load_settings() -> Settings:
    return Settings(
        github_token=_require("GITHUB_TOKEN"),
        github_repo=_require("GITHUB_REPOSITORY", default="franciscopaulinoq/zela-severiano-api"),
        openai_api_key=os.getenv("OPENAI_API_KEY"),
        openai_model=os.getenv("OPENAI_MODEL", "gpt-4o"),
        anthropic_api_key=os.getenv("ANTHROPIC_API_KEY"),
        anthropic_model=os.getenv("ANTHROPIC_MODEL", "claude-sonnet-4-5"),
        gemini_api_key=os.getenv("GEMINI_API_KEY"),
        # TEMPORARIO (calibracao, issue #21): gemini-2.5-pro foi aposentado
        # para contas novas e o tier "Pro" atual (gemini-3.1-pro-preview) tem
        # cota zero sem faturamento habilitado. Usando um modelo "flash" so
        # para validar o encanamento do pipeline - trocar para
        # gemini-3.1-pro-preview (ou o tier Pro vigente) assim que o
        # faturamento estiver configurado, antes de abrir as PRs das 20
        # issues reais avaliadas no TCC.
        gemini_model=os.getenv("GEMINI_MODEL", "gemini-3.6-flash"),
        request_timeout_seconds=float(os.getenv("PR_REVIEW_TIMEOUT_SECONDS", "60")),
        max_retries=int(os.getenv("PR_REVIEW_MAX_RETRIES", "4")),
        results_csv_path=os.getenv("PR_REVIEW_RESULTS_CSV", "results/pr_reviews.csv"),
        project_context_path=os.getenv(
            "PR_REVIEW_PROJECT_CONTEXT", str(_REPO_ROOT / "context" / "project_context.md")
        ),
    )


def _require(name: str, default: str | None = None) -> str:
    value = os.getenv(name, default)
    if not value:
        raise RuntimeError(f"Variável de ambiente obrigatória ausente: {name}")
    return value

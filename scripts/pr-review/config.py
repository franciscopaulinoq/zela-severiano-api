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

# Caminho absoluto: o workflow roda com working-directory scripts/pr-review.
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

    reasoning_effort: str
    max_output_tokens: int

    request_timeout_seconds: float
    max_retries: int

    results_csv_path: str
    comments_csv_path: str
    project_context_path: str


def load_settings() -> Settings:
    return Settings(
        github_token=_require("GITHUB_TOKEN"),
        github_repo=_require("GITHUB_REPOSITORY", default="franciscopaulinoq/zela-severiano-api"),
        openai_api_key=os.getenv("OPENAI_API_KEY"),
        # IDs fixos (nunca alias -latest/-preview), versionados no código.
        openai_model=os.getenv("OPENAI_MODEL", "gpt-6.1-sol"),
        anthropic_api_key=os.getenv("ANTHROPIC_API_KEY"),
        anthropic_model=os.getenv("ANTHROPIC_MODEL", "claude-sonnet-5-5"),
        gemini_api_key=os.getenv("GEMINI_API_KEY"),
        gemini_model=os.getenv("GEMINI_MODEL", "gemini-3.8-flash"),
        # Mesmo nível nominal de raciocínio nos três (nenhum permite
        # desligá-lo) e mesmo teto de saída, que inclui os tokens de raciocínio.
        reasoning_effort=os.getenv("PR_REVIEW_REASONING_EFFORT", "medium"),
        max_output_tokens=int(os.getenv("PR_REVIEW_MAX_OUTPUT_TOKENS", "16000")),
        request_timeout_seconds=float(os.getenv("PR_REVIEW_TIMEOUT_SECONDS", "300")),
        max_retries=int(os.getenv("PR_REVIEW_MAX_RETRIES", "4")),
        results_csv_path=os.getenv("PR_REVIEW_RESULTS_CSV", "results/pr_reviews.csv"),
        comments_csv_path=os.getenv("PR_REVIEW_COMMENTS_CSV", "results/comentarios.csv"),
        project_context_path=os.getenv(
            "PR_REVIEW_PROJECT_CONTEXT", str(_REPO_ROOT / "context" / "project_context.md")
        ),
    )


def _require(name: str, default: str | None = None) -> str:
    value = os.getenv(name, default)
    if not value:
        raise RuntimeError(f"Variável de ambiente obrigatória ausente: {name}")
    return value

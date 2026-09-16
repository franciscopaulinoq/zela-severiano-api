"""Estruturas de dados compartilhadas pelo pipeline de revisão automática."""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone


@dataclass
class ArquivoAlterado:
    caminho: str
    patch: str  # diff (hunk) do arquivo, como vem da API do GitHub
    conteudo_completo: str | None  # conteúdo integral do arquivo na revisão do PR


@dataclass
class PRContext:
    """Tudo que o script sabe sobre um PR antes de montar o prompt."""

    numero_pr: int
    titulo_pr: str
    branch: str
    diff_completo: str
    arquivos_alterados: list[ArquivoAlterado]
    numero_issue: int | None
    titulo_issue: str | None
    corpo_issue: str | None
    project_context: str
    project_rules: str


@dataclass
class ModelResponse:
    modelo: str
    pr_numero: int
    issue_numero: int | None
    comentario_gerado: str
    status: str  # "ok" | "erro" | "timeout" | "rate_limited"
    latencia_ms: int
    erro_detalhe: str | None = None
    timestamp: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())

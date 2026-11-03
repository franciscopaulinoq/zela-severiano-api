"""Estruturas de dados compartilhadas pelo pipeline de revisão automática."""
from __future__ import annotations

import uuid
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
class Comentario:
    arquivo: str
    linha: int | None
    categoria: str
    severidade: str
    comentario: str


@dataclass
class ModelResponse:
    run_id: str
    experimento: str
    modelo: str  # ID solicitado
    nivel_prompt: str  # P1 | P2 | P3
    prompt_sha256: str
    pr_numero: int
    issue_numero: int | None
    effort: str
    comentario_gerado: str  # texto bruto, exatamente como veio
    status: str  # "ok" | "erro" | "timeout" | "rate_limited"
    latencia_ms: int
    modelo_retornado: str | None = None
    motivo_parada: str | None = None
    tokens_entrada: int | None = None
    tokens_saida: int | None = None
    tokens_raciocinio: int | None = None
    json_valido: bool | None = None  # None quando a chamada falhou
    comentarios: list[Comentario] = field(default_factory=list)
    erro_detalhe: str | None = None
    revisao_id: str = field(default_factory=lambda: uuid.uuid4().hex[:12])
    timestamp: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())

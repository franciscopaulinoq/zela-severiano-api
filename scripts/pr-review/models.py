"""Estruturas de dados do pipeline."""

from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime, timezone


@dataclass
class ArquivoAlterado:
    caminho: str
    patch: str
    conteudo: str | None


@dataclass
class ContextoPR:
    numero_pr: int
    titulo_pr: str
    diff: str
    arquivos: list[ArquivoAlterado]
    numero_issue: int | None
    titulo_issue: str | None
    corpo_issue: str | None
    contexto_projeto: str


@dataclass
class Comentario:
    arquivo: str
    linha: int | None
    categoria: str
    severidade: str
    comentario: str


@dataclass
class Revisao:
    run_id: str
    experimento: str
    modelo: str
    nivel_prompt: str
    prompt_sha256: str
    pr_numero: int
    issue_numero: int | None
    effort: str
    comentario_gerado: str = ""
    status: str = "ok"
    latencia_ms: int = 0
    modelo_retornado: str | None = None
    motivo_parada: str | None = None
    tokens_entrada: int | None = None
    tokens_saida: int | None = None
    tokens_raciocinio: int | None = None
    json_valido: bool | None = None
    comentarios: list[Comentario] = field(default_factory=list)
    erro_detalhe: str | None = None
    revisao_id: str = field(default_factory=lambda: uuid.uuid4().hex[:12])
    timestamp: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())

"""Extrai a lista de comentários do texto devolvido pelo modelo.

Tolerante só ao que não muda o conteúdo: cercas ```json e texto em volta do
array. Qualquer outra coisa (JSON quebrado, objeto em vez de lista, item sem
os campos esperados) é falha de formato: `json_valido = False`, sem nova
tentativa, e a revisão conta como "nenhum comentário" (protocolo, doc 11
seção 10.1.f).
"""
from __future__ import annotations

import json
import re

from models import Comentario

_CERCA = re.compile(r"```(?:json)?\s*(.*?)```", re.DOTALL)


def extrair_comentarios(texto: str) -> tuple[bool, list[Comentario]]:
    candidato = texto.strip()
    cerca = _CERCA.search(candidato)
    if cerca:
        candidato = cerca.group(1).strip()
    inicio, fim = candidato.find("["), candidato.rfind("]")
    if inicio == -1 or fim < inicio:
        return False, []
    try:
        dados = json.loads(candidato[inicio : fim + 1])
    except json.JSONDecodeError:
        return False, []
    if not isinstance(dados, list) or not all(isinstance(item, dict) for item in dados):
        return False, []

    comentarios = []
    for item in dados:
        if not str(item.get("comentario", "")).strip():
            return False, []
        comentarios.append(
            Comentario(
                arquivo=str(item.get("arquivo", "")),
                linha=_inteiro_ou_none(item.get("linha")),
                categoria=str(item.get("categoria", "")),
                severidade=str(item.get("severidade", "")),
                comentario=str(item["comentario"]),
            )
        )
    return True, comentarios


def _inteiro_ou_none(valor: object) -> int | None:
    try:
        return int(valor)  # type: ignore[arg-type]
    except (TypeError, ValueError):
        return None

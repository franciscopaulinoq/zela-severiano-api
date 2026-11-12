"""Extrai a lista de comentários da resposta do modelo.

Aceita cercas ```json e texto em volta do array. Qualquer outro desvio do formato
(JSON inválido, objeto em vez de lista, item sem `comentario`) é falha de formato.
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
        itens = json.loads(candidato[inicio : fim + 1])
    except json.JSONDecodeError:
        return False, []
    if not isinstance(itens, list) or not all(isinstance(item, dict) for item in itens):
        return False, []
    if any(not str(item.get("comentario", "")).strip() for item in itens):
        return False, []

    return True, [
        Comentario(
            arquivo=str(item.get("arquivo", "")),
            linha=_inteiro(item.get("linha")),
            categoria=str(item.get("categoria", "")),
            severidade=str(item.get("severidade", "")),
            comentario=str(item["comentario"]),
        )
        for item in itens
    ]


def _inteiro(valor: object) -> int | None:
    try:
        return int(valor)  # type: ignore[arg-type]
    except (TypeError, ValueError):
        return None

"""Grava as revisões (uma linha por revisão) e os comentários (uma linha por comentário) em CSV."""

from __future__ import annotations

import csv
from pathlib import Path

from models import Revisao

_COLUNAS_REVISAO = [
    "timestamp",
    "run_id",
    "revisao_id",
    "experimento",
    "pr_numero",
    "issue_numero",
    "modelo",
    "modelo_retornado",
    "nivel_prompt",
    "effort",
    "prompt_sha256",
    "status",
    "motivo_parada",
    "latencia_ms",
    "tokens_entrada",
    "tokens_saida",
    "tokens_raciocinio",
    "json_valido",
    "n_comentarios",
    "comentario_gerado",
    "erro_detalhe",
]

_COLUNAS_COMENTARIO = [
    "run_id",
    "revisao_id",
    "experimento",
    "pr_numero",
    "issue_numero",
    "modelo",
    "nivel_prompt",
    "indice",
    "arquivo",
    "linha",
    "categoria",
    "severidade",
    "comentario",
]


def salvar_revisao(csv_revisoes: str, csv_comentarios: str, revisao: Revisao) -> None:
    _anexar(csv_revisoes, _COLUNAS_REVISAO, [_linha_revisao(revisao)])
    _anexar(
        csv_comentarios,
        _COLUNAS_COMENTARIO,
        [
            {
                "run_id": revisao.run_id,
                "revisao_id": revisao.revisao_id,
                "experimento": revisao.experimento,
                "pr_numero": revisao.pr_numero,
                "issue_numero": revisao.issue_numero,
                "modelo": revisao.modelo,
                "nivel_prompt": revisao.nivel_prompt,
                "indice": indice,
                "arquivo": comentario.arquivo,
                "linha": _vazio_se_nulo(comentario.linha),
                "categoria": comentario.categoria,
                "severidade": comentario.severidade,
                "comentario": comentario.comentario,
            }
            for indice, comentario in enumerate(revisao.comentarios)
        ],
    )


def _linha_revisao(revisao: Revisao) -> dict:
    json_valido = "" if revisao.json_valido is None else ("S" if revisao.json_valido else "N")
    return {
        "timestamp": revisao.timestamp,
        "run_id": revisao.run_id,
        "revisao_id": revisao.revisao_id,
        "experimento": revisao.experimento,
        "pr_numero": revisao.pr_numero,
        "issue_numero": revisao.issue_numero,
        "modelo": revisao.modelo,
        "modelo_retornado": revisao.modelo_retornado or "",
        "nivel_prompt": revisao.nivel_prompt,
        "effort": revisao.effort,
        "prompt_sha256": revisao.prompt_sha256,
        "status": revisao.status,
        "motivo_parada": revisao.motivo_parada or "",
        "latencia_ms": revisao.latencia_ms,
        "tokens_entrada": _vazio_se_nulo(revisao.tokens_entrada),
        "tokens_saida": _vazio_se_nulo(revisao.tokens_saida),
        "tokens_raciocinio": _vazio_se_nulo(revisao.tokens_raciocinio),
        "json_valido": json_valido,
        "n_comentarios": len(revisao.comentarios),
        "comentario_gerado": revisao.comentario_gerado,
        "erro_detalhe": revisao.erro_detalhe or "",
    }


def _anexar(caminho_csv: str, colunas: list[str], linhas: list[dict]) -> None:
    caminho = Path(caminho_csv)
    caminho.parent.mkdir(parents=True, exist_ok=True)
    novo = not caminho.exists()
    with caminho.open("a", newline="", encoding="utf-8") as arquivo:
        escritor = csv.DictWriter(arquivo, fieldnames=colunas)
        if novo:
            escritor.writeheader()
        escritor.writerows(linhas)


def _vazio_se_nulo(valor: object) -> object:
    return "" if valor is None else valor

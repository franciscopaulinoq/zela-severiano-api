"""Persiste os resultados em formato tabular (CSV).

Dois arquivos:
- `pr_reviews.csv`: uma linha por revisão (PR × modelo × nível de prompt ×
  nível de issue), com o texto bruto da resposta e os metadados de
  auditoria (modelo devolvido pela API, tokens, hash do prompt);
- `comentarios.csv`: uma linha por comentário extraído da resposta, ligada
  à revisão por `revisao_id`. É a entrada da planilha cega de classificação.

CSV (em vez de banco de dados) de propósito: o volume é pequeno, abre direto
em qualquer planilha e não exige infraestrutura extra no GitHub Actions.
"""
from __future__ import annotations

import csv
from pathlib import Path

from models import ModelResponse

_CAMPOS_REVISAO = [
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

_CAMPOS_COMENTARIO = [
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


def salvar_resultado(caminho_revisoes: str, caminho_comentarios: str, resposta: ModelResponse) -> None:
    _anexar(
        caminho_revisoes,
        _CAMPOS_REVISAO,
        [
            {
                "timestamp": resposta.timestamp,
                "run_id": resposta.run_id,
                "revisao_id": resposta.revisao_id,
                "experimento": resposta.experimento,
                "pr_numero": resposta.pr_numero,
                "issue_numero": resposta.issue_numero,
                "modelo": resposta.modelo,
                "modelo_retornado": resposta.modelo_retornado or "",
                "nivel_prompt": resposta.nivel_prompt,
                "effort": resposta.effort,
                "prompt_sha256": resposta.prompt_sha256,
                "status": resposta.status,
                "motivo_parada": resposta.motivo_parada or "",
                "latencia_ms": resposta.latencia_ms,
                "tokens_entrada": _vazio_se_none(resposta.tokens_entrada),
                "tokens_saida": _vazio_se_none(resposta.tokens_saida),
                "tokens_raciocinio": _vazio_se_none(resposta.tokens_raciocinio),
                "json_valido": "" if resposta.json_valido is None else ("S" if resposta.json_valido else "N"),
                "n_comentarios": len(resposta.comentarios),
                "comentario_gerado": resposta.comentario_gerado,
                "erro_detalhe": resposta.erro_detalhe or "",
            }
        ],
    )
    _anexar(
        caminho_comentarios,
        _CAMPOS_COMENTARIO,
        [
            {
                "run_id": resposta.run_id,
                "revisao_id": resposta.revisao_id,
                "experimento": resposta.experimento,
                "pr_numero": resposta.pr_numero,
                "issue_numero": resposta.issue_numero,
                "modelo": resposta.modelo,
                "nivel_prompt": resposta.nivel_prompt,
                "indice": indice,
                "arquivo": comentario.arquivo,
                "linha": _vazio_se_none(comentario.linha),
                "categoria": comentario.categoria,
                "severidade": comentario.severidade,
                "comentario": comentario.comentario,
            }
            for indice, comentario in enumerate(resposta.comentarios)
        ],
    )


def _anexar(caminho_csv: str, campos: list[str], linhas: list[dict]) -> None:
    caminho = Path(caminho_csv)
    caminho.parent.mkdir(parents=True, exist_ok=True)
    arquivo_existe = caminho.exists()
    with caminho.open("a", newline="", encoding="utf-8") as f:
        escritor = csv.DictWriter(f, fieldnames=campos)
        if not arquivo_existe:
            escritor.writeheader()
        escritor.writerows(linhas)


def _vazio_se_none(valor: object) -> object:
    return "" if valor is None else valor

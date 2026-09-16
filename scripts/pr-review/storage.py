"""Persiste os resultados de cada chamada de modelo em formato tabular (CSV).

CSV (em vez de banco de dados) de propósito: o volume é pequeno (20-30 PRs x
3 modelos = ~60-90 linhas), CSV abre direto em qualquer planilha para a
classificação manual (etapa fora do escopo deste script) e não exige
infraestrutura extra em um workflow do GitHub Actions.
"""
from __future__ import annotations

import csv
from pathlib import Path

from models import ModelResponse

_CAMPOS = [
    "timestamp",
    "pr_numero",
    "issue_numero",
    "modelo",
    "status",
    "latencia_ms",
    "comentario_gerado",
    "erro_detalhe",
]


def salvar_resultado(caminho_csv: str, resposta: ModelResponse) -> None:
    caminho = Path(caminho_csv)
    caminho.parent.mkdir(parents=True, exist_ok=True)
    arquivo_existe = caminho.exists()

    with caminho.open("a", newline="", encoding="utf-8") as f:
        escritor = csv.DictWriter(f, fieldnames=_CAMPOS)
        if not arquivo_existe:
            escritor.writeheader()
        escritor.writerow(
            {
                "timestamp": resposta.timestamp,
                "pr_numero": resposta.pr_numero,
                "issue_numero": resposta.issue_numero,
                "modelo": resposta.modelo,
                "status": resposta.status,
                "latencia_ms": resposta.latencia_ms,
                "comentario_gerado": resposta.comentario_gerado,
                "erro_detalhe": resposta.erro_detalhe or "",
            }
        )

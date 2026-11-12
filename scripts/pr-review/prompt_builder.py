"""Monta o prompt de uma revisão: instruções do nível + formato de saída + dados do PR.

Só as instruções mudam entre P1, P2 e P3; o restante é idêntico para os três modelos.
"""

from __future__ import annotations

import hashlib
from pathlib import Path

from models import ArquivoAlterado, ContextoPR

NIVEIS_PROMPT = ("P1", "P2", "P3")
_PASTA_PROMPTS = Path(__file__).parent / "prompts"


def montar_prompt(contexto: ContextoPR, nivel: str = "P2") -> str:
    if nivel not in NIVEIS_PROMPT:
        raise ValueError(f"Nível de prompt inválido: {nivel}")

    instrucoes = _ler(f"instrucoes_{nivel.lower()}.md")
    formato = _ler("formato_saida.md")
    dados = _ler("dados.md")

    valores = {
        "{{DIFF}}": contexto.diff,
        "{{ARQUIVOS_ALTERADOS_COMPLETOS}}": "\n\n".join(_formatar(arquivo) for arquivo in contexto.arquivos),
        "{{ISSUE_NUMBER}}": str(contexto.numero_issue or "N/A"),
        "{{ISSUE_TITLE}}": contexto.titulo_issue or "(nenhuma issue vinculada encontrada no corpo do PR)",
        "{{ISSUE_BODY}}": contexto.corpo_issue or "",
        "{{PROJECT_CONTEXT}}": contexto.contexto_projeto,
        "{{PR_NUMBER}}": str(contexto.numero_pr),
        "{{PR_TITLE}}": contexto.titulo_pr,
    }
    for marcador, valor in valores.items():
        dados = dados.replace(marcador, valor)

    return f"{instrucoes.strip()}\n\n{formato.strip()}\n\n---\n\n{dados}"


def sha256(texto: str) -> str:
    return hashlib.sha256(texto.encode("utf-8")).hexdigest()


def _ler(nome: str) -> str:
    return (_PASTA_PROMPTS / nome).read_text(encoding="utf-8")


def _formatar(arquivo: ArquivoAlterado) -> str:
    if arquivo.conteudo is None:
        return f"### `{arquivo.caminho}`\n(conteúdo completo indisponível — apenas o patch abaixo)\n```diff\n{arquivo.patch}\n```"
    return f"### `{arquivo.caminho}`\n```java\n{arquivo.conteudo}\n```"

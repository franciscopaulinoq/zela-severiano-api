"""Monta o prompt de uma revisão a partir do PRContext e do nível de
instrução (P1/P2/P3).

Só o bloco de instruções muda entre P1, P2 e P3. O formato de saída e os
dados (issue, contexto do projeto, arquivos completos, diff) são idênticos
em todos os níveis e para os três modelos: qualquer diferença nas respostas
vem do modelo ou do nível manipulado, não de outra parte do prompt.
"""
from __future__ import annotations

import hashlib
from pathlib import Path

from models import PRContext

_PROMPTS_DIR = Path(__file__).parent / "prompts"
NIVEIS_PROMPT = ("P1", "P2", "P3")


def montar_prompt(contexto: PRContext, nivel_prompt: str = "P2") -> str:
    if nivel_prompt not in NIVEIS_PROMPT:
        raise ValueError(f"Nível de prompt inválido: {nivel_prompt}")

    instrucoes = _ler(f"instrucoes_{nivel_prompt.lower()}.md")
    formato = _ler("formato_saida.md")
    dados = _ler("dados.md")

    arquivos_formatados = "\n\n".join(
        _formatar_arquivo(arquivo.caminho, arquivo.patch, arquivo.conteudo_completo)
        for arquivo in contexto.arquivos_alterados
    )

    substituicoes = {
        "{{DIFF}}": contexto.diff_completo,
        "{{ARQUIVOS_ALTERADOS_COMPLETOS}}": arquivos_formatados,
        "{{ISSUE_NUMBER}}": str(contexto.numero_issue or "N/A"),
        "{{ISSUE_TITLE}}": contexto.titulo_issue or "(nenhuma issue vinculada encontrada no corpo do PR)",
        "{{ISSUE_BODY}}": contexto.corpo_issue or "",
        "{{PROJECT_CONTEXT}}": contexto.project_context,
        "{{PR_NUMBER}}": str(contexto.numero_pr),
        "{{PR_TITLE}}": contexto.titulo_pr,
    }
    for chave, valor in substituicoes.items():
        dados = dados.replace(chave, valor)

    return f"{instrucoes.strip()}\n\n{formato.strip()}\n\n---\n\n{dados}"


def sha256(texto: str) -> str:
    return hashlib.sha256(texto.encode("utf-8")).hexdigest()


def _ler(nome: str) -> str:
    return (_PROMPTS_DIR / nome).read_text(encoding="utf-8")


def _formatar_arquivo(caminho: str, patch: str, conteudo_completo: str | None) -> str:
    if conteudo_completo is None:
        return f"### `{caminho}`\n(conteúdo completo indisponível — apenas o patch abaixo)\n```diff\n{patch}\n```"
    return f"### `{caminho}`\n```java\n{conteudo_completo}\n```"

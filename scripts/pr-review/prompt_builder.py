"""Preenche o template único de prompt (prompts/code_review_prompt.md) com o
PRContext de um PR específico. O mesmo template, sem nenhuma variação por
modelo, é enviado a GPT-4o, Claude e Gemini — é o que garante validade
interna à comparação (qualquer diferença nas respostas vem do modelo, não do
prompt).
"""
from __future__ import annotations

from pathlib import Path

from models import PRContext

_TEMPLATE_PATH = Path(__file__).parent / "prompts" / "code_review_prompt.md"


def montar_prompt(contexto: PRContext) -> str:
    template = _TEMPLATE_PATH.read_text(encoding="utf-8")

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

    prompt = template
    for chave, valor in substituicoes.items():
        prompt = prompt.replace(chave, valor)
    return prompt


def _formatar_arquivo(caminho: str, patch: str, conteudo_completo: str | None) -> str:
    if conteudo_completo is None:
        return f"### `{caminho}`\n(conteúdo completo indisponível — apenas o patch abaixo)\n```diff\n{patch}\n```"
    return f"### `{caminho}`\n```java\n{conteudo_completo}\n```"

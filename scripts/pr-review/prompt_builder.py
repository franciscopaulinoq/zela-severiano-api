"""Monta o prompt de uma revisão a partir do PRContext, do nível de
instrução (P1/P2/P3) e do nível de completude da issue (I1/I2/I3).

Só o bloco de instruções muda entre P1, P2 e P3. O formato de saída e os
dados (issue, contexto do projeto, arquivos completos, diff) são idênticos
em todos os níveis e para os três modelos: qualquer diferença nas respostas
vem do modelo ou do nível manipulado, não de outra parte do prompt.
"""
from __future__ import annotations

import hashlib
import re
from pathlib import Path

from models import PRContext

_PROMPTS_DIR = Path(__file__).parent / "prompts"
NIVEIS_PROMPT = ("P1", "P2", "P3")
NIVEIS_ISSUE = ("I1", "I2", "I3")

# I2 mantém só estas seções da issue (comparação pelo início do título da seção).
_SECOES_I2 = ("contexto", "requisito")
_TITULO_SECAO = re.compile(r"^##\s+(.*)$", re.MULTILINE)


def montar_prompt(contexto: PRContext, nivel_prompt: str = "P2", nivel_issue: str = "I3") -> str:
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
        "{{ISSUE_BODY}}": degradar_issue(contexto.corpo_issue or "", nivel_issue),
        "{{PROJECT_CONTEXT}}": contexto.project_context,
        "{{PR_NUMBER}}": str(contexto.numero_pr),
        "{{PR_TITLE}}": contexto.titulo_pr,
    }
    for chave, valor in substituicoes.items():
        dados = dados.replace(chave, valor)

    return f"{instrucoes.strip()}\n\n{formato.strip()}\n\n---\n\n{dados}"


def degradar_issue(corpo: str, nivel_issue: str) -> str:
    """Regra fixa de completude da issue (o título é sempre mantido):
    I3 = corpo inteiro; I2 = só as seções Contexto e Requisito; I1 = sem corpo."""
    if nivel_issue == "I3":
        return corpo
    if nivel_issue == "I1":
        return ""
    if nivel_issue != "I2":
        raise ValueError(f"Nível de issue inválido: {nivel_issue}")

    secoes = []
    titulos = list(_TITULO_SECAO.finditer(corpo))
    for i, titulo in enumerate(titulos):
        fim = titulos[i + 1].start() if i + 1 < len(titulos) else len(corpo)
        if titulo.group(1).strip().lower().startswith(_SECOES_I2):
            secoes.append(corpo[titulo.start() : fim].strip())
    return "\n\n".join(secoes)


def sha256(texto: str) -> str:
    return hashlib.sha256(texto.encode("utf-8")).hexdigest()


def _ler(nome: str) -> str:
    return (_PROMPTS_DIR / nome).read_text(encoding="utf-8")


def _formatar_arquivo(caminho: str, patch: str, conteudo_completo: str | None) -> str:
    if conteudo_completo is None:
        return f"### `{caminho}`\n(conteúdo completo indisponível — apenas o patch abaixo)\n```diff\n{patch}\n```"
    return f"### `{caminho}`\n```java\n{conteudo_completo}\n```"

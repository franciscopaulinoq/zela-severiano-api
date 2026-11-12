"""Monta o contexto de um PR: diff, conteúdo completo dos arquivos alterados, issue
vinculada e contexto do projeto."""

from __future__ import annotations

from pathlib import Path

from config import Configuracao
from github_client import ClienteGitHub
from models import ArquivoAlterado, ContextoPR

# acima disso, o arquivo vai só como patch
_TAMANHO_MAXIMO_ARQUIVO = 20_000


def montar_contexto(config: Configuracao, github: ClienteGitHub, numero_pr: int) -> ContextoPR:
    pr = github.obter_pr(numero_pr)
    sha = pr["head"]["sha"]
    arquivos = [
        ArquivoAlterado(arquivo["filename"], arquivo.get("patch", ""), _conteudo(github, arquivo, sha))
        for arquivo in github.listar_arquivos(numero_pr)
    ]

    numero_issue = ClienteGitHub.numero_da_issue(pr.get("body"))
    issue = github.obter_issue(numero_issue) if numero_issue is not None else {}

    return ContextoPR(
        numero_pr=numero_pr,
        titulo_pr=pr.get("title", ""),
        diff=github.obter_diff(numero_pr),
        arquivos=arquivos,
        numero_issue=numero_issue,
        titulo_issue=issue.get("title"),
        corpo_issue=issue.get("body"),
        contexto_projeto=_ler_contexto_projeto(config.caminho_contexto),
    )


def _conteudo(github: ClienteGitHub, arquivo: dict, sha: str) -> str | None:
    if arquivo.get("status") == "removed":
        return None
    conteudo = github.obter_conteudo(arquivo["filename"], sha)
    if conteudo is not None and len(conteudo) > _TAMANHO_MAXIMO_ARQUIVO:
        return None
    return conteudo


def _ler_contexto_projeto(caminho: str) -> str:
    arquivo = Path(caminho)
    if not arquivo.exists():
        return "(contexto do projeto não encontrado — rode build_project_context.py antes de executar a revisão)"
    return arquivo.read_text(encoding="utf-8")

"""Monta o PRContext completo: diff + conteúdo integral dos arquivos tocados
+ contexto estático do projeto (gerado por build_project_context.py).

Por que não mandar só o diff (hunk)? Um hunk isolado não mostra o resto do
arquivo (ex.: os imports, os outros métodos da classe, se já existe uma
validação parecida duas linhas acima do hunk). Sem isso os modelos tendem a
"alucinar" — supor que uma validação não existe, ou sugerir um método que já
foi definido fora do trecho alterado. Mandamos o arquivo inteiro (na versão
do PR) ao lado do diff justamente para ancorar a análise no código real.
"""
from __future__ import annotations

from pathlib import Path

from config import Settings
from github_client import GitHubClient
from models import ArquivoAlterado, PRContext

_TAMANHO_MAXIMO_ARQUIVO = 20_000  # caracteres; acima disso, só o patch é enviado


def montar_contexto(settings: Settings, github: GitHubClient, numero_pr: int) -> PRContext:
    pr = github.get_pull_request(numero_pr)
    diff_completo = github.get_pull_request_diff(numero_pr)
    arquivos_da_api = github.get_pull_request_files(numero_pr)

    ref_head = pr["head"]["sha"]
    arquivos_alterados = [
        ArquivoAlterado(
            caminho=arquivo["filename"],
            patch=arquivo.get("patch", ""),
            conteudo_completo=_conteudo_completo_ou_none(github, arquivo, ref_head),
        )
        for arquivo in arquivos_da_api
    ]

    numero_issue = GitHubClient.extrair_numero_issue(pr.get("body"))
    titulo_issue = corpo_issue = None
    if numero_issue is not None:
        issue = github.get_issue(numero_issue)
        titulo_issue = issue.get("title")
        corpo_issue = issue.get("body")

    project_context = _ler_contexto_do_projeto(settings)

    return PRContext(
        numero_pr=numero_pr,
        titulo_pr=pr.get("title", ""),
        branch=pr["head"]["ref"],
        diff_completo=diff_completo,
        arquivos_alterados=arquivos_alterados,
        numero_issue=numero_issue,
        titulo_issue=titulo_issue,
        corpo_issue=corpo_issue,
        project_context=project_context,
        project_rules="",  # já incorporado em project_context (ver build_project_context.py)
    )


def _conteudo_completo_ou_none(github: GitHubClient, arquivo: dict, ref_head: str) -> str | None:
    if arquivo.get("status") == "removed":
        return None
    conteudo = github.get_file_content(arquivo["filename"], ref_head)
    if conteudo is not None and len(conteudo) > _TAMANHO_MAXIMO_ARQUIVO:
        return None  # arquivo grande demais: o modelo fica só com o patch
    return conteudo


def _ler_contexto_do_projeto(settings: Settings) -> str:
    caminho = Path(settings.project_context_path)
    if not caminho.exists():
        return (
            "(contexto do projeto não encontrado — rode build_project_context.py "
            "antes de executar a revisão)"
        )
    return caminho.read_text(encoding="utf-8")

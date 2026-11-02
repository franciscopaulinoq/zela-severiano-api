"""Gera um snapshot estático do contexto do projeto (context/project_context.md).

Motivação: pedir para os modelos revisarem só o diff faz eles alucinarem
sobre a arquitetura ao redor (ex.: sugerir algo que já existe em outra
camada, ou não perceber que aquele pacote é "domain" e não pode depender
de Spring). Este script roda avulso (não a cada PR — só quando a estrutura
do projeto muda de verdade) e produz um arquivo único que é anexado a todo
prompt.

Uso: `python build_project_context.py [--raiz <checkout do projeto>]`. Sem
`--raiz`, usa este próprio repositório. O workflow de coleta passa o checkout
do commit do PR, para o contexto refletir o projeto como ele estava naquele PR.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

CAMADAS = ["domain", "application", "adapter", "infrastructure"]


def montar_arvore_pacotes(src_root: Path) -> str:
    linhas: list[str] = []
    for camada in CAMADAS:
        raiz_camada = next(src_root.rglob(camada), None)
        if raiz_camada is None or not raiz_camada.is_dir():
            continue
        linhas.append(f"### `{camada}/`")
        for pacote in sorted(p for p in raiz_camada.rglob("*") if p.is_dir()):
            arquivos = sorted(f.name for f in pacote.glob("*.java"))
            if not arquivos:
                continue
            pacote_relativo = pacote.relative_to(raiz_camada).as_posix()
            linhas.append(f"- `{camada}/{pacote_relativo}`: {', '.join(arquivos)}")
        linhas.append("")
    return "\n".join(linhas)


def ler_ou_vazio(caminho: Path) -> str:
    return caminho.read_text(encoding="utf-8") if caminho.exists() else ""


_SECAO_H2 = re.compile(r"^## .*$", re.MULTILINE)
MARCADOR_OMITIR = "<!-- omitir-do-contexto-de-revisao -->"


def sem_descricao_do_pipeline(markdown: str) -> str:
    """Remove do texto o que descreve o próprio pipeline de revisão, e não o
    projeto: seções `## ` precedidas do marcador `MARCADOR_OMITIR` e o bloco
    de citação (`> ...`) logo no início do arquivo, que explica o uso do
    arquivo pelo pipeline."""
    linhas = markdown.splitlines()
    # bloco de citação logo depois do título principal
    i = 1 if linhas and linhas[0].startswith("# ") else 0
    while i < len(linhas) and not linhas[i].strip():
        i += 1
    j = i
    while j < len(linhas) and linhas[j].startswith(">"):
        j += 1
    if j > i:
        linhas = linhas[:i] + linhas[j:]
    texto = "\n".join(linhas)

    partes = []
    posicoes = [m.start() for m in _SECAO_H2.finditer(texto)] + [len(texto)]
    partes.append(texto[: posicoes[0]])
    for inicio, fim in zip(posicoes, posicoes[1:]):
        secao = texto[inicio:fim]
        if not texto[:inicio].rstrip().endswith(MARCADOR_OMITIR):
            partes.append(secao)
    return "".join(partes).replace(MARCADOR_OMITIR, "").strip() + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--raiz", type=Path, default=REPO_ROOT, help="Raiz do checkout do projeto")
    raiz = parser.parse_args().raiz.resolve()
    saida = raiz / "context" / "project_context.md"

    partes = [
        "# Contexto do Projeto — Zela Severiano API\n",
        "> Gerado automaticamente por `build_project_context.py`. Não editar à mão.\n",
        "## README (visão geral e arquitetura)\n",
        sem_descricao_do_pipeline(ler_ou_vazio(raiz / "README.md")),
        "\n## Regras de projeto e convenções\n",
        sem_descricao_do_pipeline(ler_ou_vazio(raiz / "docs" / "public" / "regras_projeto.md")),
        "\n## Estrutura de pacotes (Clean Architecture)\n",
        montar_arvore_pacotes(raiz / "src" / "main" / "java"),
    ]

    saida.parent.mkdir(parents=True, exist_ok=True)
    saida.write_text("\n".join(partes), encoding="utf-8")
    print(f"Contexto do projeto gerado em {saida}")


if __name__ == "__main__":
    main()

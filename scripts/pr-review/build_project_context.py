"""Gera `context/project_context.md`: README, regras do projeto e árvore de pacotes.

Uso: python build_project_context.py [--raiz <checkout do projeto>]
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

RAIZ_REPOSITORIO = Path(__file__).resolve().parents[2]
CAMADAS = ["domain", "application", "adapter", "infrastructure"]
MARCADOR_OMITIR = "<!-- omitir-do-contexto-de-revisao -->"

_SECAO_H2 = re.compile(r"^## .*$", re.MULTILINE)


def montar_arvore_pacotes(raiz_codigo: Path) -> str:
    linhas: list[str] = []
    for camada in CAMADAS:
        raiz_camada = next(raiz_codigo.rglob(camada), None)
        if raiz_camada is None or not raiz_camada.is_dir():
            continue
        linhas.append(f"### `{camada}/`")
        for pacote in sorted(p for p in raiz_camada.rglob("*") if p.is_dir()):
            arquivos = sorted(f.name for f in pacote.glob("*.java"))
            if arquivos:
                linhas.append(f"- `{camada}/{pacote.relative_to(raiz_camada).as_posix()}`: {', '.join(arquivos)}")
        linhas.append("")
    return "\n".join(linhas)


def ler_ou_vazio(caminho: Path) -> str:
    return caminho.read_text(encoding="utf-8") if caminho.exists() else ""


def sem_descricao_do_pipeline(markdown: str) -> str:
    """Remove o bloco de citação logo abaixo do título e as seções `## ` precedidas de
    `MARCADOR_OMITIR`, que descrevem o pipeline e não o projeto."""
    linhas = markdown.splitlines()
    inicio = 1 if linhas and linhas[0].startswith("# ") else 0
    while inicio < len(linhas) and not linhas[inicio].strip():
        inicio += 1
    fim = inicio
    while fim < len(linhas) and linhas[fim].startswith(">"):
        fim += 1
    texto = "\n".join(linhas[:inicio] + linhas[fim:])

    posicoes = [m.start() for m in _SECAO_H2.finditer(texto)] + [len(texto)]
    partes = [texto[: posicoes[0]]]
    for comeco, final in zip(posicoes, posicoes[1:]):
        if not texto[:comeco].rstrip().endswith(MARCADOR_OMITIR):
            partes.append(texto[comeco:final])
    return "".join(partes).replace(MARCADOR_OMITIR, "").strip() + "\n"


def montar_contexto_projeto(raiz: Path) -> str:
    return "\n".join(
        [
            "# Contexto do Projeto — Zela Severiano API\n",
            "> Gerado automaticamente por `build_project_context.py`. Não editar à mão.\n",
            "## README (visão geral e arquitetura)\n",
            sem_descricao_do_pipeline(ler_ou_vazio(raiz / "README.md")),
            "\n## Regras de projeto e convenções\n",
            sem_descricao_do_pipeline(ler_ou_vazio(raiz / "docs" / "public" / "regras_projeto.md")),
            "\n## Estrutura de pacotes (Clean Architecture)\n",
            montar_arvore_pacotes(raiz / "src" / "main" / "java"),
        ]
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Gera o contexto do projeto enviado aos modelos.")
    parser.add_argument("--raiz", type=Path, default=RAIZ_REPOSITORIO, help="raiz do checkout do projeto")
    raiz = parser.parse_args().raiz.resolve()

    saida = raiz / "context" / "project_context.md"
    saida.parent.mkdir(parents=True, exist_ok=True)
    saida.write_text(montar_contexto_projeto(raiz), encoding="utf-8")
    print(f"Contexto do projeto gerado em {saida}")


if __name__ == "__main__":
    main()

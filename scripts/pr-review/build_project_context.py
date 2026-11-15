"""Gera `context/project_context.md`: README, regras do projeto e árvore do código-fonte.

Uso: python build_project_context.py [--raiz <checkout>] [--regras <arquivo>] [--fontes <pasta>]
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

from prompt_builder import rebaixar_titulos

RAIZ_REPOSITORIO = Path(__file__).resolve().parents[2]
REGRAS_PADRAO = Path("docs/public/regras_projeto.md")
FONTES_PADRAO = Path("src/main/java")
MARCADOR_OMITIR = "<!-- omitir-do-contexto-de-revisao -->"

_SECAO_H2 = re.compile(r"^## .*$", re.MULTILINE)


def montar_arvore_fontes(raiz_fontes: Path) -> str:
    pastas = sorted({f.parent for f in raiz_fontes.rglob("*") if f.is_file()})
    if not pastas:
        return ""
    base = Path(*_prefixo_comum([pasta.relative_to(raiz_fontes).parts for pasta in pastas]))
    linhas = []
    for pasta in pastas:
        arquivos = sorted(f.name for f in pasta.iterdir() if f.is_file())
        relativa = pasta.relative_to(raiz_fontes / base).as_posix()
        linhas.append(f"- `{relativa}/`: {', '.join(arquivos)}")
    return "\n".join(linhas) + "\n"


def _prefixo_comum(caminhos: list[tuple[str, ...]]) -> tuple[str, ...]:
    prefixo = caminhos[0]
    for partes in caminhos[1:]:
        tamanho = 0
        while tamanho < min(len(prefixo), len(partes)) and prefixo[tamanho] == partes[tamanho]:
            tamanho += 1
        prefixo = prefixo[:tamanho]
    return prefixo


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


def montar_contexto_projeto(raiz: Path, regras: Path = REGRAS_PADRAO, fontes: Path = FONTES_PADRAO) -> str:
    return "\n".join(
        [
            "## README (visão geral e arquitetura)\n",
            rebaixar_titulos(sem_descricao_do_pipeline(ler_ou_vazio(raiz / "README.md")), 3),
            "\n## Regras de projeto e convenções\n",
            rebaixar_titulos(sem_descricao_do_pipeline(ler_ou_vazio(raiz / regras)), 3),
            f"\n## Estrutura do código-fonte (`{fontes.as_posix()}`)\n",
            montar_arvore_fontes(raiz / fontes),
        ]
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Gera o contexto do projeto enviado aos modelos.")
    parser.add_argument("--raiz", type=Path, default=RAIZ_REPOSITORIO, help="raiz do checkout do projeto")
    parser.add_argument("--regras", type=Path, default=REGRAS_PADRAO, help="regras do projeto, relativo à raiz")
    parser.add_argument("--fontes", type=Path, default=FONTES_PADRAO, help="pasta do código-fonte, relativo à raiz")
    argumentos = parser.parse_args()
    raiz = argumentos.raiz.resolve()

    saida = raiz / "context" / "project_context.md"
    saida.parent.mkdir(parents=True, exist_ok=True)
    saida.write_text(montar_contexto_projeto(raiz, argumentos.regras, argumentos.fontes), encoding="utf-8")
    print(f"Contexto do projeto gerado em {saida}")


if __name__ == "__main__":
    main()

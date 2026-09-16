"""Gera um snapshot estático do contexto do projeto (context/project_context.md).

Motivação: pedir para os modelos revisarem só o diff faz eles alucinarem
sobre a arquitetura ao redor (ex.: sugerir algo que já existe em outra
camada, ou não perceber que aquele pacote é "domain" e não pode depender
de Spring). Este script roda avulso (não a cada PR — só quando a estrutura
do projeto muda de verdade) e produz um arquivo único que é anexado a todo
prompt.

Uso: `python build_project_context.py` a partir da raiz do repositório.
"""
from __future__ import annotations

from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SRC_ROOT = REPO_ROOT / "src" / "main" / "java"
README_PATH = REPO_ROOT / "README.md"
PROJECT_RULES_PATH = REPO_ROOT / "docs" / "public" / "regras_projeto.md"
OUTPUT_PATH = REPO_ROOT / "context" / "project_context.md"

CAMADAS = ["domain", "application", "adapter", "infrastructure"]


def montar_arvore_pacotes() -> str:
    linhas: list[str] = []
    for camada in CAMADAS:
        raiz_camada = next(SRC_ROOT.rglob(camada), None)
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


def main() -> None:
    partes = [
        "# Contexto do Projeto — Zela Severiano API\n",
        "> Gerado automaticamente por `build_project_context.py`. Não editar à mão.\n",
        "## README (visão geral e arquitetura)\n",
        ler_ou_vazio(README_PATH),
        "\n## Regras de projeto e convenções\n",
        ler_ou_vazio(PROJECT_RULES_PATH),
        "\n## Estrutura de pacotes (Clean Architecture)\n",
        montar_arvore_pacotes(),
    ]

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text("\n".join(partes), encoding="utf-8")
    print(f"Contexto do projeto gerado em {OUTPUT_PATH}")


if __name__ == "__main__":
    main()

"""Chamadas à API REST do GitHub usadas pelo pipeline."""

from __future__ import annotations

import base64
import re

import requests

from config import Configuracao

_API = "https://api.github.com"
_REFERENCIA_ISSUE = re.compile(r"\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#(\d+)", re.IGNORECASE)


class ClienteGitHub:
    def __init__(self, config: Configuracao):
        self._repositorio = config.repositorio
        self._sessao = requests.Session()
        self._sessao.headers.update(
            {
                "Authorization": f"Bearer {config.token_github}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
            }
        )

    def obter_pr(self, numero: int) -> dict:
        return self._get(f"pulls/{numero}").json()

    def obter_diff(self, numero: int) -> str:
        return self._get(f"pulls/{numero}", headers={"Accept": "application/vnd.github.v3.diff"}).text

    def listar_arquivos(self, numero: int) -> list[dict]:
        arquivos: list[dict] = []
        pagina = 1
        while True:
            lote = self._get(f"pulls/{numero}/files", params={"per_page": 100, "page": pagina}).json()
            arquivos.extend(lote)
            if len(lote) < 100:
                return arquivos
            pagina += 1

    def obter_conteudo(self, caminho: str, ref: str) -> str | None:
        resposta = self._sessao.get(self._url(f"contents/{caminho}"), params={"ref": ref})
        if resposta.status_code == 404:
            return None
        resposta.raise_for_status()
        dados = resposta.json()
        if dados.get("encoding") != "base64":
            return None
        return base64.b64decode(dados["content"]).decode("utf-8", errors="replace")

    def obter_issue(self, numero: int) -> dict:
        return self._get(f"issues/{numero}").json()

    @staticmethod
    def numero_da_issue(corpo_pr: str | None) -> int | None:
        encontrado = _REFERENCIA_ISSUE.search(corpo_pr or "")
        return int(encontrado.group(1)) if encontrado else None

    def _url(self, caminho: str) -> str:
        return f"{_API}/repos/{self._repositorio}/{caminho}"

    def _get(self, caminho: str, **kwargs) -> requests.Response:
        resposta = self._sessao.get(self._url(caminho), **kwargs)
        resposta.raise_for_status()
        return resposta

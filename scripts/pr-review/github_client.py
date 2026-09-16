"""Cliente fino sobre a API REST do GitHub.

Usamos `requests` puro (em vez de PyGithub) de propósito: o pipeline só
precisa de meia dúzia de chamadas (PR, diff, arquivos, conteúdo, issue,
comentário) e uma dependência a menos é mais fácil de auditar/justificar
numa monografia.
"""
from __future__ import annotations

import base64
import re

import requests

from config import Settings

_API_BASE = "https://api.github.com"
_ISSUE_REF_PATTERN = re.compile(
    r"\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#(\d+)", re.IGNORECASE
)


class GitHubClient:
    def __init__(self, settings: Settings):
        self._settings = settings
        self._session = requests.Session()
        self._session.headers.update(
            {
                "Authorization": f"Bearer {settings.github_token}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
            }
        )

    def _repo_url(self, path: str) -> str:
        return f"{_API_BASE}/repos/{self._settings.github_repo}/{path}"

    def get_pull_request(self, numero_pr: int) -> dict:
        resp = self._session.get(self._repo_url(f"pulls/{numero_pr}"))
        resp.raise_for_status()
        return resp.json()

    def get_pull_request_diff(self, numero_pr: int) -> str:
        resp = self._session.get(
            self._repo_url(f"pulls/{numero_pr}"),
            headers={"Accept": "application/vnd.github.v3.diff"},
        )
        resp.raise_for_status()
        return resp.text

    def get_pull_request_files(self, numero_pr: int) -> list[dict]:
        arquivos: list[dict] = []
        pagina = 1
        while True:
            resp = self._session.get(
                self._repo_url(f"pulls/{numero_pr}/files"),
                params={"per_page": 100, "page": pagina},
            )
            resp.raise_for_status()
            lote = resp.json()
            arquivos.extend(lote)
            if len(lote) < 100:
                break
            pagina += 1
        return arquivos

    def get_file_content(self, caminho: str, ref: str) -> str | None:
        resp = self._session.get(
            self._repo_url(f"contents/{caminho}"), params={"ref": ref}
        )
        if resp.status_code == 404:
            return None  # arquivo removido no PR, por exemplo
        resp.raise_for_status()
        conteudo = resp.json()
        if conteudo.get("encoding") != "base64":
            return None
        return base64.b64decode(conteudo["content"]).decode("utf-8", errors="replace")

    def get_issue(self, numero_issue: int) -> dict:
        resp = self._session.get(self._repo_url(f"issues/{numero_issue}"))
        resp.raise_for_status()
        return resp.json()

    def post_comment(self, numero_pr: int, corpo: str) -> None:
        resp = self._session.post(
            self._repo_url(f"issues/{numero_pr}/comments"), json={"body": corpo}
        )
        resp.raise_for_status()

    @staticmethod
    def extrair_numero_issue(corpo_pr: str | None) -> int | None:
        """Extrai o número da issue referenciada via 'closes #N' / 'fixes #N' etc."""
        if not corpo_pr:
            return None
        match = _ISSUE_REF_PATTERN.search(corpo_pr)
        return int(match.group(1)) if match else None

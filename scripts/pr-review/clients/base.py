"""Interface comum implementada pelos três clientes de modelo.

Cada cliente só sabe transformar `prompt -> texto de resposta`. Retry,
timeout, medição de latência e persistência ficam fora dele (em
review_pr.py) para que a política de resiliência seja uma só, aplicada de
forma idêntica aos três modelos — não uma por SDK.
"""
from __future__ import annotations

from abc import ABC, abstractmethod


class ModelClient(ABC):
    nome: str

    @abstractmethod
    async def gerar(self, prompt: str, timeout_seconds: float) -> str:
        """Retorna o texto bruto de resposta do modelo, ou levanta uma exceção."""
        raise NotImplementedError

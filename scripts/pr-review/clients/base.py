"""Interface comum implementada pelos três clientes de modelo.

Cada cliente só sabe transformar `prompt -> RespostaBruta`. Retry,
timeout, medição de latência e persistência ficam fora dele (em
review_pr.py) para que a política de resiliência seja uma só, aplicada de
forma idêntica aos três modelos — não uma por SDK.

Configuração de inferência (decisão D6 do protocolo): temperatura padrão de
cada provedor (nenhum cliente envia `temperature`), mesmo nível nominal de
raciocínio e mesmo limite de tokens de saída nos três.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass


@dataclass
class RespostaBruta:
    texto: str
    modelo_retornado: str | None  # versão que a API diz ter usado (auditoria de deriva)
    motivo_parada: str | None
    tokens_entrada: int | None
    tokens_saida: int | None  # inclui raciocínio nos três provedores
    tokens_raciocinio: int | None


class ModelClient(ABC):
    nome: str

    @abstractmethod
    async def gerar(self, prompt: str, timeout_seconds: float) -> RespostaBruta:
        """Retorna a resposta do modelo, ou levanta uma exceção."""
        raise NotImplementedError

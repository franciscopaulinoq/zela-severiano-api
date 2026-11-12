"""Interface comum dos clientes de modelo.

Retry, timeout, latência e gravação ficam em review_pr.py, iguais para os três. Nenhum
cliente envia `temperature`: vale o padrão de cada provedor.
"""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass


@dataclass
class RespostaModelo:
    texto: str
    modelo_retornado: str | None
    motivo_parada: str | None
    tokens_entrada: int | None
    tokens_saida: int | None
    tokens_raciocinio: int | None


class ClienteModelo(ABC):
    def __init__(self, modelo: str, esforco: str, max_tokens_saida: int):
        self.modelo = modelo
        self._esforco = esforco
        self._max_tokens_saida = max_tokens_saida

    @abstractmethod
    async def gerar(self, prompt: str, timeout_segundos: float) -> RespostaModelo: ...

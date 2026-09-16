from __future__ import annotations

import asyncio

from google import genai

from clients.base import ModelClient


class GeminiClient(ModelClient):
    def __init__(self, api_key: str, model: str):
        self._client = genai.Client(api_key=api_key)
        self._model = model
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> str:
        # O SDK google-genai ainda não tem um cliente assíncrono estável para
        # todos os métodos; rodamos a chamada síncrona numa thread para não
        # bloquear o loop de eventos e conseguirmos aplicar o mesmo timeout
        # usado nos outros dois modelos.
        return await asyncio.wait_for(
            asyncio.to_thread(self._gerar_sincrono, prompt), timeout=timeout_seconds
        )

    def _gerar_sincrono(self, prompt: str) -> str:
        resposta = self._client.models.generate_content(
            model=self._model,
            contents=prompt,
            config={"temperature": 0.2},
        )
        return resposta.text or ""

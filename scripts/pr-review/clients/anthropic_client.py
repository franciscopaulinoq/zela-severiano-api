from __future__ import annotations

from anthropic import AsyncAnthropic

from clients.base import ModelClient


class AnthropicClient(ModelClient):
    def __init__(self, api_key: str, model: str):
        self._client = AsyncAnthropic(api_key=api_key)
        self._model = model
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> str:
        resposta = await self._client.messages.create(
            model=self._model,
            max_tokens=4096,
            temperature=0.2,
            messages=[{"role": "user", "content": prompt}],
            timeout=timeout_seconds,
        )
        return "".join(bloco.text for bloco in resposta.content if bloco.type == "text")

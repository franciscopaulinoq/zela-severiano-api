from __future__ import annotations

from openai import AsyncOpenAI

from clients.base import ModelClient


class OpenAIClient(ModelClient):
    nome = "gpt-4o"

    def __init__(self, api_key: str, model: str):
        self._client = AsyncOpenAI(api_key=api_key)
        self._model = model
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> str:
        resposta = await self._client.chat.completions.create(
            model=self._model,
            messages=[{"role": "user", "content": prompt}],
            temperature=0.2,
            timeout=timeout_seconds,
        )
        return resposta.choices[0].message.content or ""

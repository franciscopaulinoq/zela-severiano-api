from __future__ import annotations

from anthropic import AsyncAnthropic

from clients.base import ModelClient


class AnthropicClient(ModelClient):
    def __init__(self, api_key: str, model: str):
        self._client = AsyncAnthropic(api_key=api_key)
        self._model = model
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> str:
        # SDKs >=1.x removeram `temperature` como kwarg tipado de
        # messages.create (a API continua aceitando o campo normalmente),
        # por isso vai via extra_body em vez de parametro nomeado.
        resposta = await self._client.messages.create(
            model=self._model,
            max_tokens=4096,
            messages=[{"role": "user", "content": prompt}],
            timeout=timeout_seconds,
            extra_body={"temperature": 0.2},
        )
        return "".join(bloco.text for bloco in resposta.content if bloco.type == "text")

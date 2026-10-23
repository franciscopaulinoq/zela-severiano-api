from __future__ import annotations

from openai import AsyncOpenAI

from clients.base import ModelClient, RespostaBruta


class OpenAIClient(ModelClient):
    def __init__(self, api_key: str, model: str, effort: str, max_output_tokens: int):
        self._client = AsyncOpenAI(api_key=api_key)
        self._model = model
        self._effort = effort
        self._max_output_tokens = max_output_tokens
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> RespostaBruta:
        resposta = await self._client.chat.completions.create(
            model=self._model,
            messages=[{"role": "user", "content": prompt}],
            reasoning_effort=self._effort,
            max_completion_tokens=self._max_output_tokens,
            timeout=timeout_seconds,
        )
        escolha = resposta.choices[0]
        uso = resposta.usage
        detalhes = uso.completion_tokens_details if uso else None
        return RespostaBruta(
            texto=escolha.message.content or "",
            modelo_retornado=resposta.model,
            motivo_parada=escolha.finish_reason,
            tokens_entrada=uso.prompt_tokens if uso else None,
            tokens_saida=uso.completion_tokens if uso else None,
            tokens_raciocinio=detalhes.reasoning_tokens if detalhes else None,
        )

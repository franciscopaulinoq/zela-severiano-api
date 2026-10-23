from __future__ import annotations

from anthropic import AsyncAnthropic

from clients.base import ModelClient, RespostaBruta


class AnthropicClient(ModelClient):
    def __init__(self, api_key: str, model: str, effort: str, max_output_tokens: int):
        self._client = AsyncAnthropic(api_key=api_key)
        self._model = model
        self._effort = effort
        self._max_output_tokens = max_output_tokens
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> RespostaBruta:
        # Claude 4.7+ recusa (400) temperature/top_p/top_k fora do padrão, e o
        # raciocínio não pode ser desligado: fica adaptativo, e a profundidade
        # é controlada só pelo `effort`.
        resposta = await self._client.messages.create(
            model=self._model,
            max_tokens=self._max_output_tokens,
            messages=[{"role": "user", "content": prompt}],
            thinking={"type": "adaptive"},
            output_config={"effort": self._effort},
            timeout=timeout_seconds,
        )
        uso = resposta.usage
        detalhes = uso.output_tokens_details
        return RespostaBruta(
            texto="".join(bloco.text for bloco in resposta.content if bloco.type == "text"),
            modelo_retornado=resposta.model,
            motivo_parada=resposta.stop_reason,
            tokens_entrada=uso.input_tokens,
            tokens_saida=uso.output_tokens,
            tokens_raciocinio=detalhes.thinking_tokens if detalhes else None,
        )

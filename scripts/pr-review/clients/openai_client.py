from __future__ import annotations

from openai import AsyncOpenAI

from clients.base import ClienteModelo, RespostaModelo


class ClienteOpenAI(ClienteModelo):
    def __init__(self, chave: str, modelo: str, esforco: str, max_tokens_saida: int):
        super().__init__(modelo, esforco, max_tokens_saida)
        self._api = AsyncOpenAI(api_key=chave)

    async def gerar(self, prompt: str, timeout_segundos: float) -> RespostaModelo:
        resposta = await self._api.chat.completions.create(
            model=self.modelo,
            messages=[{"role": "user", "content": prompt}],
            reasoning_effort=self._esforco,
            max_completion_tokens=self._max_tokens_saida,
            timeout=timeout_segundos,
        )
        escolha = resposta.choices[0]
        uso = resposta.usage
        detalhes = uso.completion_tokens_details if uso else None
        return RespostaModelo(
            texto=escolha.message.content or "",
            modelo_retornado=resposta.model,
            motivo_parada=escolha.finish_reason,
            tokens_entrada=uso.prompt_tokens if uso else None,
            tokens_saida=uso.completion_tokens if uso else None,
            tokens_raciocinio=detalhes.reasoning_tokens if detalhes else None,
        )

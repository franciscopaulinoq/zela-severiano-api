from __future__ import annotations

import asyncio

from google import genai
from google.genai import types

from clients.base import ModelClient, RespostaBruta


class GeminiClient(ModelClient):
    def __init__(self, api_key: str, model: str, effort: str, max_output_tokens: int):
        self._client = genai.Client(api_key=api_key)
        self._model = model
        self._thinking_level = types.ThinkingLevel(effort.upper())
        self._max_output_tokens = max_output_tokens
        self.nome = model

    async def gerar(self, prompt: str, timeout_seconds: float) -> RespostaBruta:
        # Chamada síncrona numa thread para aplicar o mesmo timeout dos outros clientes.
        return await asyncio.wait_for(
            asyncio.to_thread(self._gerar_sincrono, prompt), timeout=timeout_seconds
        )

    def _gerar_sincrono(self, prompt: str) -> RespostaBruta:
        # Sem `temperature`: o Gemini 3 recomenda manter o padrão (1.0).
        resposta = self._client.models.generate_content(
            model=self._model,
            contents=prompt,
            config=types.GenerateContentConfig(
                max_output_tokens=self._max_output_tokens,
                thinking_config=types.ThinkingConfig(thinking_level=self._thinking_level),
            ),
        )
        uso = resposta.usage_metadata
        candidato = resposta.candidates[0] if resposta.candidates else None
        saida = None
        if uso and uso.candidates_token_count is not None:
            saida = uso.candidates_token_count + (uso.thoughts_token_count or 0)
        return RespostaBruta(
            texto=resposta.text or "",
            modelo_retornado=resposta.model_version,
            motivo_parada=str(candidato.finish_reason) if candidato and candidato.finish_reason else None,
            tokens_entrada=uso.prompt_token_count if uso else None,
            tokens_saida=saida,
            tokens_raciocinio=uso.thoughts_token_count if uso else None,
        )

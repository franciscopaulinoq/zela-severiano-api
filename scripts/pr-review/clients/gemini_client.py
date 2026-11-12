from __future__ import annotations

import asyncio

from google import genai
from google.genai import types

from clients.base import ClienteModelo, RespostaModelo


class ClienteGemini(ClienteModelo):
    def __init__(self, chave: str, modelo: str, esforco: str, max_tokens_saida: int):
        super().__init__(modelo, esforco, max_tokens_saida)
        self._api = genai.Client(api_key=chave)
        self._nivel_raciocinio = types.ThinkingLevel(esforco.upper())

    async def gerar(self, prompt: str, timeout_segundos: float) -> RespostaModelo:
        return await asyncio.wait_for(self._gerar(prompt), timeout=timeout_segundos)

    async def _gerar(self, prompt: str) -> RespostaModelo:
        resposta = await self._api.aio.models.generate_content(
            model=self.modelo,
            contents=prompt,
            config=types.GenerateContentConfig(
                max_output_tokens=self._max_tokens_saida,
                thinking_config=types.ThinkingConfig(thinking_level=self._nivel_raciocinio),
                automatic_function_calling=types.AutomaticFunctionCallingConfig(disable=True),
            ),
        )
        uso = resposta.usage_metadata
        candidato = resposta.candidates[0] if resposta.candidates else None
        # a API separa resposta e raciocínio; somados, ficam comparáveis aos outros provedores
        tokens_saida = None
        if uso and uso.candidates_token_count is not None:
            tokens_saida = uso.candidates_token_count + (uso.thoughts_token_count or 0)
        return RespostaModelo(
            texto=resposta.text or "",
            modelo_retornado=resposta.model_version,
            motivo_parada=str(candidato.finish_reason) if candidato and candidato.finish_reason else None,
            tokens_entrada=uso.prompt_token_count if uso else None,
            tokens_saida=tokens_saida,
            tokens_raciocinio=uso.thoughts_token_count if uso else None,
        )

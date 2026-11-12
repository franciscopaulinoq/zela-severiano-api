from __future__ import annotations

from anthropic import AsyncAnthropic

from clients.base import ClienteModelo, RespostaModelo


class ClienteAnthropic(ClienteModelo):
    def __init__(self, chave: str, modelo: str, esforco: str, max_tokens_saida: int):
        super().__init__(modelo, esforco, max_tokens_saida)
        self._api = AsyncAnthropic(api_key=chave)

    async def gerar(self, prompt: str, timeout_segundos: float) -> RespostaModelo:
        # o raciocínio não pode ser desligado neste modelo; o esforço controla a profundidade
        resposta = await self._api.messages.create(
            model=self.modelo,
            max_tokens=self._max_tokens_saida,
            messages=[{"role": "user", "content": prompt}],
            thinking={"type": "adaptive"},
            output_config={"effort": self._esforco},
            timeout=timeout_segundos,
        )
        uso = resposta.usage
        detalhes = uso.output_tokens_details
        return RespostaModelo(
            texto="".join(bloco.text for bloco in resposta.content if bloco.type == "text"),
            modelo_retornado=resposta.model,
            motivo_parada=resposta.stop_reason,
            tokens_entrada=uso.input_tokens,
            tokens_saida=uso.output_tokens,
            tokens_raciocinio=detalhes.thinking_tokens if detalhes else None,
        )

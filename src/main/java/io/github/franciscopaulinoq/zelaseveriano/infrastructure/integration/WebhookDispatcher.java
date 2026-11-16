package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import io.github.franciscopaulinoq.zelaseveriano.application.service.WebhookService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Webhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EntregaWebhookRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.WebhookRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDispatcher {
  private static final int LOTE_ENTREGAS = 100;

  private final WebhookRepository webhookRepository;
  private final EntregaWebhookRepository entregaRepository;
  private final WebhookService webhookService;
  private final WebhookHttpClient httpClient;
  private final Clock clock;

  @Scheduled(
      initialDelayString = "${api.webhooks.initial-delay:1000}",
      fixedDelayString = "${api.webhooks.intervalo:1000}")
  public void processar() {
    Map<UUID, Webhook> ativos =
        webhookRepository.findAllByAtivoTrue().stream()
            .collect(Collectors.toMap(Webhook::getId, Function.identity()));
    for (UUID id : ativos.keySet()) {
      try {
        webhookService.sincronizar(id);
      } catch (RuntimeException exception) {
        log.error("Falha ao sincronizar eventos do webhook {}", id, exception);
      }
    }

    for (EntregaWebhook entrega :
        entregaRepository.findPendentes(OffsetDateTime.now(clock), LOTE_ENTREGAS)) {
      Webhook destino = ativos.get(entrega.getWebhookId());
      if (destino == null) {
        continue;
      }
      tentarEnviar(entrega, destino);
    }
  }

  private void tentarEnviar(EntregaWebhook entrega, Webhook destino) {
    OffsetDateTime agora = OffsetDateTime.now(clock);
    try {
      httpClient.enviar(entrega, destino.getUrl());
      entrega.registrarSucesso(agora);
    } catch (RuntimeException exception) {
      entrega.registrarFalha(agora, exception.getMessage());
      log.warn(
          "Falha na entrega {} do webhook {}; nova tentativa em {}",
          entrega.getId(),
          destino.getId(),
          entrega.getProximaTentativaEm());
    }
    entregaRepository.save(entrega);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import io.github.franciscopaulinoq.zelaseveriano.domain.integration.WebhookDestinationValidator;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class WebhookHttpClient {
  private static final int RESPOSTA_MAXIMA = 64 * 1024;

  private final ObjectMapper objectMapper;
  private final WebhookDestinationValidator destinationValidator;
  private final Duration timeout;
  private final HttpClient httpClient;

  public WebhookHttpClient(
      ObjectMapper objectMapper,
      WebhookDestinationValidator destinationValidator,
      @Value("${api.webhooks.timeout:3s}") Duration timeout) {
    this.objectMapper = objectMapper;
    this.destinationValidator = destinationValidator;
    this.timeout = timeout;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(timeout)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  public void enviar(EntregaWebhook entrega, String url) {
    destinationValidator.validar(url);
    try {
      String json =
          objectMapper.writeValueAsString(
              new EventoWebhook(
                  entrega.getId(),
                  entrega.getRelatoId(),
                  entrega.getStatusAnterior(),
                  entrega.getStatusNovo(),
                  entrega.getMomentoMudanca()));
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(url))
              .timeout(timeout)
              .header("Content-Type", "application/json")
              .header("X-Entrega-Id", entrega.getId().toString())
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();
      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      try (InputStream corpo = response.body()) {
        if (corpo.readNBytes(RESPOSTA_MAXIMA + 1).length > RESPOSTA_MAXIMA) {
          throw new IOException("Resposta do webhook excede o limite.");
        }
      }
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IOException("Destino respondeu HTTP " + response.statusCode());
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Envio do webhook interrompido.", exception);
    } catch (IOException | RuntimeException exception) {
      throw new IllegalStateException("Não foi possível entregar o webhook.", exception);
    }
  }

  private record EventoWebhook(
      UUID entregaId,
      long relatoId,
      StatusRelato statusAnterior,
      StatusRelato statusNovo,
      OffsetDateTime momentoMudanca) {}
}

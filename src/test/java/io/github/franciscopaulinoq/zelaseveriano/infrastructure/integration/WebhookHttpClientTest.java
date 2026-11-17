package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sun.net.httpserver.HttpServer;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.WebhookDestinationValidator;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class WebhookHttpClientTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private HttpServer servidor;

  @AfterEach
  void encerrar() {
    if (servidor != null) {
      servidor.stop(0);
    }
  }

  @Test
  void enviaJsonComEventoEIdentificadorDaEntrega() throws Exception {
    AtomicReference<String> corpo = new AtomicReference<>();
    AtomicReference<String> cabecalho = new AtomicReference<>();
    servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    servidor.createContext(
        "/eventos",
        exchange -> {
          corpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          cabecalho.set(exchange.getRequestHeaders().getFirst("X-Entrega-Id"));
          exchange.sendResponseHeaders(204, -1);
          exchange.close();
        });
    servidor.start();
    WebhookHttpClient cliente =
        new WebhookHttpClient(
            objectMapper, mock(WebhookDestinationValidator.class), Duration.ofSeconds(1));
    EntregaWebhook entrega = entrega();

    cliente.enviar(entrega, "http://127.0.0.1:" + servidor.getAddress().getPort() + "/eventos");

    JsonNode json = objectMapper.readTree(corpo.get());
    assertThat(json.get("entregaId").asText()).isEqualTo(entrega.getId().toString());
    assertThat(json.get("relatoId").asLong()).isEqualTo(42);
    assertThat(json.get("statusAnterior").asText()).isEqualTo("PENDENTE");
    assertThat(json.get("statusNovo").asText()).isEqualTo("EM_ANALISE");
    assertThat(json.get("momentoMudanca").asText()).isEqualTo("2026-01-10T12:00:00Z");
    assertThat(cabecalho.get()).isEqualTo(entrega.getId().toString());
  }

  private EntregaWebhook entrega() {
    return EntregaWebhook.builder()
        .id(UUID.randomUUID())
        .webhookId(UUID.randomUUID())
        .historicoId(1)
        .relatoId(42)
        .statusAnterior(StatusRelato.PENDENTE)
        .statusNovo(StatusRelato.EM_ANALISE)
        .momentoMudanca(OffsetDateTime.parse("2026-01-10T12:00:00Z"))
        .proximaTentativaEm(OffsetDateTime.parse("2026-01-10T12:00:00Z"))
        .build();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class RecuperacaoSenhaHttpGatewayTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private HttpServer server;

  @AfterEach
  void pararServidor() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void enviaEmailECodigoEmJson() throws IOException {
    AtomicReference<String> corpo = new AtomicReference<>();
    iniciarServidor(204, corpo);

    gateway().enviar("maria@example.com", "123456");

    JsonNode json = objectMapper.readTree(corpo.get());
    assertThat(json.get("email").asText()).isEqualTo("maria@example.com");
    assertThat(json.get("codigo").asText()).isEqualTo("123456");
  }

  @Test
  void respostaDeErroDoServicoEhRecusada() throws IOException {
    iniciarServidor(500, new AtomicReference<>());

    assertThatThrownBy(() -> gateway().enviar("maria@example.com", "123456"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Não foi possível entregar");
  }

  private RecuperacaoSenhaHttpGateway gateway() {
    return new RecuperacaoSenhaHttpGateway(
        "http://localhost:" + server.getAddress().getPort() + "/recuperacao",
        Duration.ofSeconds(1),
        objectMapper);
  }

  private void iniciarServidor(int status, AtomicReference<String> corpo) throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/recuperacao",
        exchange -> {
          corpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          exchange.sendResponseHeaders(status, -1);
          exchange.close();
        });
    server.start();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.IntegracaoCepIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ViaCepGatewayTest {
  private HttpServer server;
  private ViaCepGateway gateway;
  private int status;
  private String corpo;
  private String caminho;

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/",
        exchange -> {
          caminho = exchange.getRequestURI().getPath();
          byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(status, bytes.length);
          try {
            exchange.getResponseBody().write(bytes);
          } finally {
            exchange.close();
          }
        });
    server.start();
    gateway =
        new ViaCepGateway(
            "http://localhost:" + server.getAddress().getPort(),
            Duration.ofSeconds(1),
            new ObjectMapper());
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void respostaCompletaEhConvertidaParaEndereco() {
    responder(
        200,
        """
        {"logradouro":"Rua Principal","bairro":"Centro","localidade":"Doutor Severiano","uf":"RN"}
        """);

    Optional<EnderecoCep> resultado = gateway.consultar("59680000");

    assertThat(resultado)
        .contains(new EnderecoCep("Rua Principal", "Centro", "Doutor Severiano", "RN"));
    assertThat(caminho).isEqualTo("/59680000/json/");
  }

  @Test
  void cepAusenteNaoProduzEndereco() {
    responder(200, "{\"erro\":true}");

    assertThat(gateway.consultar("59680999")).isEmpty();
  }

  @Test
  void falhaHttpEhInformadaComoIndisponibilidade() {
    responder(503, "{}");

    assertThatThrownBy(() -> gateway.consultar("59680000"))
        .isInstanceOf(IntegracaoCepIndisponivelException.class)
        .hasMessageContaining("Tente novamente");
  }

  private void responder(int status, String corpo) {
    this.status = status;
    this.corpo = corpo;
  }
}

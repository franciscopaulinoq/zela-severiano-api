package io.github.franciscopaulinoq.zelaseveriano.infrastructure.notification;

import io.github.franciscopaulinoq.zelaseveriano.domain.notification.ConfirmacaoEmailGateway;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ConfirmacaoEmailHttpGateway implements ConfirmacaoEmailGateway {
  private final URI endpoint;
  private final Duration timeout;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public ConfirmacaoEmailHttpGateway(
      @Value("${api.confirmacao-email.entrega-url}") String endpoint,
      @Value("${api.confirmacao-email.timeout:2s}") Duration timeout,
      ObjectMapper objectMapper) {
    this.endpoint = URI.create(endpoint);
    this.timeout = timeout;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
  }

  @Override
  public void enviar(String email, String codigo) {
    try {
      String corpo = objectMapper.writeValueAsString(new Mensagem(email, codigo));
      HttpRequest request =
          HttpRequest.newBuilder(endpoint)
              .timeout(timeout)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(corpo))
              .build();
      HttpResponse<Void> response =
          httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Serviço de entrega recusou o código de confirmação.");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Entrega do código de confirmação interrompida.", exception);
    } catch (IOException | RuntimeException exception) {
      throw new IllegalStateException(
          "Não foi possível entregar o código de confirmação.", exception);
    }
  }

  private record Mensagem(String email, String codigo) {}
}

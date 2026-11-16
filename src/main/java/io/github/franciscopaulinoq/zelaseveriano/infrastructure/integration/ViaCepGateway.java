package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.IntegracaoCepIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.CepGateway;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ViaCepGateway implements CepGateway {
  private final String urlBase;
  private final Duration timeout;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public ViaCepGateway(
      @Value("${api.integracoes.cep.url}") String urlBase,
      @Value("${api.integracoes.cep.timeout}") Duration timeout,
      ObjectMapper objectMapper) {
    this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    this.timeout = timeout;
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<EnderecoCep> consultar(String cep) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(urlBase + "/" + cep + "/json/"))
            .timeout(timeout)
            .GET()
            .build();
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 404) {
        return Optional.empty();
      }
      if (response.statusCode() != 200) {
        throw new IOException("Resposta HTTP " + response.statusCode());
      }
      ViaCepResponse dados = objectMapper.readValue(response.body(), ViaCepResponse.class);
      if (Boolean.TRUE.equals(dados.erro())) {
        return Optional.empty();
      }
      if (dados.incompleta()) {
        throw new IOException("Resposta sem todos os campos de endereço");
      }
      return Optional.of(
          new EnderecoCep(dados.logradouro(), dados.bairro(), dados.localidade(), dados.uf()));
    } catch (IOException ex) {
      throw new IntegracaoCepIndisponivelException(ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IntegracaoCepIndisponivelException(ex);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ViaCepResponse(
      String logradouro, String bairro, String localidade, String uf, Boolean erro) {
    private boolean incompleta() {
      return logradouro == null || bairro == null || localidade == null || uf == null;
    }
  }
}

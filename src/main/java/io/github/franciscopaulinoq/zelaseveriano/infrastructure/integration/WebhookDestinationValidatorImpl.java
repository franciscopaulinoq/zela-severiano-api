package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import io.github.franciscopaulinoq.zelaseveriano.domain.integration.WebhookDestinationValidator;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class WebhookDestinationValidatorImpl implements WebhookDestinationValidator {
  @Override
  public void validar(String destino) {
    try {
      URI uri = URI.create(destino);
      if (!uri.isAbsolute()
          || !("http".equalsIgnoreCase(uri.getScheme())
              || "https".equalsIgnoreCase(uri.getScheme()))
          || uri.getHost() == null
          || uri.getUserInfo() != null) {
        throw new IllegalArgumentException("URL do webhook inválida.");
      }
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("URL do webhook inválida.", exception);
    }
  }
}

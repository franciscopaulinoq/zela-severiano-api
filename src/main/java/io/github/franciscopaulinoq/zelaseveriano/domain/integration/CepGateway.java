package io.github.franciscopaulinoq.zelaseveriano.domain.integration;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;
import java.util.Optional;

public interface CepGateway {
  Optional<EnderecoCep> consultar(String cep);
}

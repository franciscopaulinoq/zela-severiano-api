package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConsultaCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EnderecoCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CepNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.CepGateway;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultaCepService {
  private final CepGateway cepGateway;
  private final Map<String, EnderecoCepDTO> cache = new ConcurrentHashMap<>();

  public EnderecoCepDTO consultar(ConsultaCepDTO dados) {
    String cep = dados.cep().replace("-", "");
    return cache.computeIfAbsent(cep, this::buscar);
  }

  private EnderecoCepDTO buscar(String cep) {
    return cepGateway
        .consultar(cep)
        .map(EnderecoCepDTO::de)
        .orElseThrow(CepNaoEncontradoException::new);
  }
}

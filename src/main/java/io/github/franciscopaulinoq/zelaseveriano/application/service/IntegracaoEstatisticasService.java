package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegracaoEstatisticasService {

  private final RelatoService relatoService;

  public EstatisticasRelatosDTO obterEstatisticas() {
    return relatoService.obterEstatisticas();
  }
}

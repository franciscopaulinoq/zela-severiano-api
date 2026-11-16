package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;

public record AvisoLeitura(Long id, Aviso aviso, Perfil perfil, OffsetDateTime lidaEm) {
  public AvisoLeitura marcarComoLida(OffsetDateTime momento) {
    return lidaEm == null ? new AvisoLeitura(id, aviso, perfil, momento) : this;
  }
}

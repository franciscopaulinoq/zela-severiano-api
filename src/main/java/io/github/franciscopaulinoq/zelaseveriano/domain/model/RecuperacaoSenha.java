package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RecuperacaoSenha {
  private UUID id;
  private UUID usuarioId;
  private String codigo;
  private OffsetDateTime expiraEm;
  private OffsetDateTime invalidadoEm;
  private OffsetDateTime criadoEm;

  public boolean expirado(OffsetDateTime momento) {
    return !momento.isBefore(expiraEm);
  }

  public void invalidar(OffsetDateTime momento) {
    this.invalidadoEm = momento;
  }
}

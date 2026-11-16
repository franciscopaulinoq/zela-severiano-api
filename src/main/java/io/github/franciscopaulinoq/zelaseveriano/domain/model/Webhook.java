package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Webhook {
  private UUID id;
  private String url;
  private boolean ativo;
  private OffsetDateTime criadoEm;
  private long ultimoEventoId;

  public void inativar() {
    ativo = false;
  }

  public void avancarCursor(long eventoId) {
    ultimoEventoId = Math.max(ultimoEventoId, eventoId);
  }
}

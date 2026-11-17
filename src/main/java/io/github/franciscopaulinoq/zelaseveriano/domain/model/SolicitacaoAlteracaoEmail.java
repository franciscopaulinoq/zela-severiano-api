package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SolicitacaoAlteracaoEmail {
  private UUID id;
  private UUID perfilId;
  private String emailSolicitado;
  private String codigoHash;
  private OffsetDateTime criadoEm;
  private OffsetDateTime expiraEm;
  private OffsetDateTime consumidoEm;

  public boolean estaExpirada(OffsetDateTime agora) {
    return !agora.isBefore(expiraEm);
  }

  public void consumir(OffsetDateTime agora) {
    consumidoEm = agora;
  }
}

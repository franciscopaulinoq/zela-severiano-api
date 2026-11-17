package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Vistoria {
  private Long id;
  private Long relatoId;
  private UUID gestorId;
  private OffsetDateTime inicio;
  private OffsetDateTime fim;
  private StatusVistoria status;
  private OffsetDateTime criadoEm;

  public static Vistoria agendar(
      Long relatoId, UUID gestorId, OffsetDateTime inicio, OffsetDateTime fim) {
    if (inicio == null || fim == null || !inicio.isBefore(fim)) {
      throw new IllegalArgumentException("O início da vistoria deve ser anterior ao fim.");
    }
    return Vistoria.builder()
        .relatoId(relatoId)
        .gestorId(gestorId)
        .inicio(inicio)
        .fim(fim)
        .status(StatusVistoria.ATIVA)
        .build();
  }

  public void cancelar() {
    this.status = StatusVistoria.CANCELADA;
  }
}

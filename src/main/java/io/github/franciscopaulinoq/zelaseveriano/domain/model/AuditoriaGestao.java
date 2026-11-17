package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuditoriaGestao {
  private Long id;
  private UUID gestorId;
  private String metodo;
  private String caminho;
  private OffsetDateTime momento;
  private Integer status;
}

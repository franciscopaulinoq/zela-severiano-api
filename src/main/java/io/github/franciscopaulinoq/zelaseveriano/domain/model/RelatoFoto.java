package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RelatoFoto {
  private Long id;
  private Long relatoId;
  private UUID autorId;
  private String urlFoto;
  private OffsetDateTime criadoEm;
}

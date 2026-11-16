package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatoCriadoDTO {
  private Long id;
  private StatusRelato status;
  private String urlFoto;
  private OffsetDateTime criadoEm;
}

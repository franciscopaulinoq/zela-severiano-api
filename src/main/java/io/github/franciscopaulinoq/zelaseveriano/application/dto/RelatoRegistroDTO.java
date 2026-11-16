package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotNull;
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
public class RelatoRegistroDTO {
  @NotNull(message = "Categoria ID é obrigatória")
  private Integer categoriaId;

  private String descricao;

  @NotNull(message = "Latitude é obrigatória")
  private Double latitude;

  @NotNull(message = "Longitude é obrigatória")
  private Double longitude;
}

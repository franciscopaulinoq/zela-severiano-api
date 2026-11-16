package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AtualizarStatusRelatoDTO {

  @NotNull(message = "Status é obrigatório")
  private StatusRelato status;

  @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
  private String observacao;
}

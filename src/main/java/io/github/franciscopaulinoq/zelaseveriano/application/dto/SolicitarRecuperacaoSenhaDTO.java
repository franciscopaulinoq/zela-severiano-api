package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarRecuperacaoSenhaDTO {
  @NotBlank(message = "CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
  private String cpf;
}

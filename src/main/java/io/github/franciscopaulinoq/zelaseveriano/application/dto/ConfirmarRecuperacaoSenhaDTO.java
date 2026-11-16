package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarRecuperacaoSenhaDTO {
  @NotBlank(message = "CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
  private String cpf;

  @NotBlank(message = "Código é obrigatório")
  @Pattern(regexp = "\\d{6}", message = "Código deve conter 6 dígitos")
  private String codigo;

  @NotBlank(message = "Nova senha é obrigatória")
  @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
  private String novaSenha;
}

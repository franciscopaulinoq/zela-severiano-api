package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  @NotBlank(message = "CPF é obrigatório")
  private String cpf;

  @NotBlank(message = "Senha é obrigatória")
  @Size(max = 72, message = "Senha deve ter no máximo 72 caracteres")
  private String senha;
}

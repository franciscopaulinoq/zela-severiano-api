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
public class TrocarSenhaRequest {
  @NotBlank(message = "Senha atual é obrigatória")
  @Size(max = 72, message = "Senha atual deve ter no máximo 72 caracteres")
  private String senhaAtual;

  @NotBlank(message = "Nova senha é obrigatória")
  @Size(min = 6, max = 72, message = "Nova senha deve ter entre 6 e 72 caracteres")
  private String novaSenha;
}

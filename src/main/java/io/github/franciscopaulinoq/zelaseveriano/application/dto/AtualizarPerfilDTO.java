package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
public class AtualizarPerfilDTO {
  @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
  private String nomeCompleto;

  @Email(message = "Email deve ser válido")
  @Pattern(regexp = ".*\\S.*", message = "Email não pode ser vazio")
  @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
  private String email;
}

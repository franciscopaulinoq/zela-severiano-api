package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegistrarCidadaoDTO {
  @NotBlank(message = "CPF é obrigatório")
  @Size(min = 11, max = 11, message = "CPF deve conter exatamente 11 dígitos")
  private String cpf;

  @NotBlank(message = "Senha é obrigatória")
  @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
  private String senha;

  @NotBlank(message = "Nome completo é obrigatório")
  @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
  private String nomeCompleto;

  @Email(message = "Email deve ser válido")
  @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
  private String email;

  @NotBlank(message = "Logradouro é obrigatório")
  @Size(min = 3, max = 150, message = "Logradouro deve ter entre 3 e 150 caracteres")
  private String logradouro;

  @NotBlank(message = "Número é obrigatório")
  @Size(min = 1, max = 20, message = "Número deve ter entre 1 e 20 caracteres")
  private String numero;

  @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
  private String complemento;

  @NotBlank(message = "Bairro é obrigatório")
  @Size(min = 3, max = 100, message = "Bairro deve ter entre 3 e 100 caracteres")
  private String bairro;
}

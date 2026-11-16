package io.github.franciscopaulinoq.zelaseveriano.application.dto;

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
  private String cpf;
  private String nomeCompleto;
  private String email;
  private String logradouro;
  private String numero;
  private String complemento;
  private String bairro;
}

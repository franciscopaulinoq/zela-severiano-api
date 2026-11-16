package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;

public record EnderecoCepDTO(String logradouro, String bairro, String cidade, String uf) {
  public static EnderecoCepDTO de(EnderecoCep endereco) {
    return new EnderecoCepDTO(
        endereco.logradouro(), endereco.bairro(), endereco.cidade(), endereco.uf());
  }
}

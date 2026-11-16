package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class GestorDesenvolvimentoForaDoPerfilIntegrationTest {

  @Autowired private UsuarioRepository usuarioRepository;

  @Test
  void gestorConhecidoNaoExisteForaDoPerfilDeDesenvolvimento() {
    assertThat(usuarioRepository.findByCpf("11144477735")).isEmpty();
  }
}

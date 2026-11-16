package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PerfilNormalizacaoTest {

  @Test
  void atualizacaoRemoveEspacosDasExtremidadesENormalizaEmail() {
    Perfil perfil = perfil();

    perfil.atualizarDados("  Maria da Silva  ", "  MARIA@EXAMPLE.COM  ");

    assertThat(perfil.getNomeCompleto()).isEqualTo("Maria da Silva");
    assertThat(perfil.getEmail()).isEqualTo("maria@example.com");
  }

  @Test
  void atualizacaoApenasDoNomePreservaEmail() {
    Perfil perfil = perfil();

    perfil.atualizarDados("Maria Souza", null);

    assertThat(perfil.getNomeCompleto()).isEqualTo("Maria Souza");
    assertThat(perfil.getEmail()).isEqualTo("maria@example.com");
  }

  @Test
  void atualizacaoApenasDoEmailPreservaNome() {
    Perfil perfil = perfil();

    perfil.atualizarDados(null, " NOVO@EXAMPLE.COM ");

    assertThat(perfil.getNomeCompleto()).isEqualTo("Maria da Silva");
    assertThat(perfil.getEmail()).isEqualTo("novo@example.com");
  }

  @Test
  void normalizacaoPreservaEspacosInternos() {
    Perfil perfil = perfil();

    perfil.atualizarDados("  Maria  da Silva  ", " MARIA@EXAMPLE  .COM ");

    assertThat(perfil.getNomeCompleto()).isEqualTo("Maria  da Silva");
    assertThat(perfil.getEmail()).isEqualTo("maria@example  .com");
  }

  private static Perfil perfil() {
    return Perfil.builder().nomeCompleto("Maria da Silva").email("maria@example.com").build();
  }
}

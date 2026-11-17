package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.SessaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TokenInvalidoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RevogacaoRenovacaoSenhaIntegrationTest {

  @Autowired private ContaService contaService;
  @Autowired private TokenService tokenService;
  @Autowired private CidadaosDeTeste cidadaos;

  private Cidadao cidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Marcos Dantas");
  }

  @Test
  void refreshAnteriorATrocaDeSenhaEhRecusado() {
    SessaoDTO sessao = login(CidadaosDeTeste.SENHA);

    trocarSenha(CidadaosDeTeste.SENHA, "nova-senha-123");

    assertThatThrownBy(() -> contaService.renovarSessao(sessao.tokenRenovacao()))
        .isInstanceOf(TokenInvalidoException.class);
  }

  @Test
  void novoLoginEmiteRefreshCapazDeRenovar() {
    trocarSenha(CidadaosDeTeste.SENHA, "nova-senha-123");
    SessaoDTO novaSessao = login("nova-senha-123");

    String token = contaService.renovarSessao(novaSessao.tokenRenovacao());

    assertThat(tokenService.validarToken(token)).isPresent();
  }

  @Test
  void trocaRecusadaPreservaRefreshAnterior() {
    SessaoDTO sessao = login(CidadaosDeTeste.SENHA);

    assertThatThrownBy(() -> trocarSenha("senha-errada", "nova-senha-123"))
        .isInstanceOf(IllegalArgumentException.class);

    assertThat(contaService.renovarSessao(sessao.tokenRenovacao())).isNotBlank();
  }

  @Test
  void trocaDeSenhaRevogaTodasAsSessoesAnteriores() {
    SessaoDTO primeira = login(CidadaosDeTeste.SENHA);
    SessaoDTO segunda = login(CidadaosDeTeste.SENHA);

    trocarSenha(CidadaosDeTeste.SENHA, "nova-senha-123");

    assertThatThrownBy(() -> contaService.renovarSessao(primeira.tokenRenovacao()))
        .isInstanceOf(TokenInvalidoException.class);
    assertThatThrownBy(() -> contaService.renovarSessao(segunda.tokenRenovacao()))
        .isInstanceOf(TokenInvalidoException.class);
  }

  private SessaoDTO login(String senha) {
    return contaService.autenticar(cidadao.cpf(), senha);
  }

  private void trocarSenha(String atual, String nova) {
    contaService.trocarSenha(cidadao.usuarioId(), atual, nova);
  }
}

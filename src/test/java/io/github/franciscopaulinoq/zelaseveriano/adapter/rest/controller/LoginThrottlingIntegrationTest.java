package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.security.LoginAttemptService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class LoginThrottlingIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private LoginAttemptService loginAttemptService;

  private Cidadao cidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Beatriz Rocha");
    loginAttemptService.registrarSucesso(cidadao.cpf());
  }

  @Test
  void sextaTentativaRecebe429NoFormatoDeErroDaApi() throws Exception {
    repetirFalhas(5);

    login("senha-errada")
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
        .andExpect(
            jsonPath("$.message").value("Muitas tentativas de login. Tente novamente mais tarde."))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void loginValidoAntesDoBloqueioLimpaFalhasAnteriores() throws Exception {
    repetirFalhas(4);
    login(CidadaosDeTeste.SENHA).andExpect(status().isOk());
    repetirFalhas(4);

    login(CidadaosDeTeste.SENHA).andExpect(status().isOk());
  }

  @Test
  void bloqueioDeUmCpfNaoAfetaOutro() throws Exception {
    Cidadao outro = cidadaos.cadastrar("Carlos Alberto");
    repetirFalhas(5);

    login("senha-errada").andExpect(status().isTooManyRequests());
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoLogin(outro.cpf(), CidadaosDeTeste.SENHA)))
        .andExpect(status().isOk());
  }

  private void repetirFalhas(int quantidade) throws Exception {
    for (int tentativa = 0; tentativa < quantidade; tentativa++) {
      login("senha-errada").andExpect(status().isUnauthorized());
    }
  }

  private ResultActions login(String senha) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpoLogin(cidadao.cpf(), senha)));
  }

  private String corpoLogin(String cpf, String senha) {
    return "{\"cpf\":\"%s\",\"senha\":\"%s\"}".formatted(cpf, senha);
  }
}

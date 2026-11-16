package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
@ExtendWith(OutputCaptureExtension.class)
class LoginAuditoriaIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  private Cidadao cidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Beatriz Rocha");
  }

  @Test
  void registraSucessoComMomentoEIdentificacao(CapturedOutput saida) throws Exception {
    login(CidadaosDeTeste.SENHA).andExpect(status().isOk());
    assertThat(saida)
        .contains(
            "evento=auditoria_login resultado=sucesso",
            "momento=",
            "cpf=***.***." + cidadao.cpf().substring(6, 9) + "-**");
  }

  @Test
  void registraFalhaSemAlterarRespostaGenerica(CapturedOutput saida) throws Exception {
    login("senha-errada")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("CPF ou senha inválidos"));
    assertThat(saida).contains("evento=auditoria_login resultado=falha", "motivo=autenticacao");

    mockMvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    assertThat(saida).containsOnlyOnce("evento=auditoria_login");
  }

  @Test
  void registraJsonMalformadoComoFalhaDeFormato(CapturedOutput saida) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{cpf:"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Requisição inválida"));
    assertThat(saida).contains("evento=auditoria_login resultado=falha", "motivo=formato");
  }

  private ResultActions login(String senha) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cpf\":\"%s\",\"senha\":\"%s\"}".formatted(cidadao.cpf(), senha)));
  }
}

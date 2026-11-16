package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CpfsDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class CadastroCidadaoIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void senhaAcimaDoLimiteEhRecusada() throws Exception {
    cadastrar("senha-secreta", "x".repeat(73))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.validation_errors.senha").exists());
  }

  @Test
  void emailAcimaDoLimiteEhRecusado() throws Exception {
    cadastrar("a".repeat(245) + "@example.com", "senha-secreta")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.validation_errors.email").exists());
  }

  private ResultActions cadastrar(String email, String senha) throws Exception {
    return mockMvc.perform(
        post("/api/v1/cidadaos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                  "cpf": "%s",
                  "senha": "%s",
                  "nomeCompleto": "Cidadão de Teste",
                  "email": "%s",
                  "logradouro": "Rua Principal",
                  "numero": "10",
                  "bairro": "Centro"
                }
                """
                    .formatted(CpfsDeTeste.novo(), senha, email)));
  }
}

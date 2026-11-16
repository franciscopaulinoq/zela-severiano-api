package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class IntegracaoEstatisticasIntegrationTest {

  private static final String URL = "/api/v1/integracoes/estatisticas";
  private static final String CHAVE = "chave-de-integracao-somente-para-testes";

  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void chaveValidaRetornaOsMesmosTotaisDaGestao() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Helena Souza");
    relatoService.registrar(
        cidadao.usuarioId(),
        new RelatoRegistroDTO(1, "Relato de teste", -6.08, -38.38),
        null,
        null,
        null);
    String esperado = estatisticasDaGestao();

    mockMvc
        .perform(get(URL).header("X-API-Key", CHAVE))
        .andExpect(status().isOk())
        .andExpect(content().json(esperado));
  }

  @Test
  void chaveInvalidaRecebe401SemEstatisticas() throws Exception {
    mockMvc
        .perform(get(URL).header("X-API-Key", "incorreta"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.total").doesNotExist());
  }

  @Test
  void ausenciaDeChaveRecebe401() throws Exception {
    mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
  }

  @Test
  void chaveValidaNaoConcedeAcessoAdministrativo() throws Exception {
    mockMvc
        .perform(get("/api/v1/gestao/relatos").header("X-API-Key", CHAVE))
        .andExpect(status().isForbidden());
  }

  private String estatisticasDaGestao() throws Exception {
    return mockMvc
        .perform(
            get("/api/v1/gestao/relatos/estatisticas")
                .header("Authorization", "Bearer " + cidadaos.tokenDeNovoGestor()))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}

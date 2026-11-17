package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ReclassificacaoRelatosOpenApiIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void endpointDeReclassificacaoEstaDocumentado() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/v1/gestao/relatos/reclassificacao'].patch").exists());
  }

  @Test
  void limiteDoLoteEstaDocumentadoNoSchema() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.components.schemas.ReclassificacaoRelatosDTO.properties.relatoIds.maxItems")
                .value(100));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoWebhookIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void gestorCadastraListaEInativaWebhook() throws Exception {
    String gestor = cidadaos.tokenDeNovoGestor();
    String resposta =
        mockMvc
            .perform(cadastrar(gestor, "https://93.184.216.34/eventos"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ativo").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.id");

    mockMvc
        .perform(get("/api/v1/gestao/webhooks").header("Authorization", "Bearer " + gestor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(id));
    mockMvc
        .perform(
            patch("/api/v1/gestao/webhooks/{id}/inativacao", id)
                .header("Authorization", "Bearer " + gestor))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/v1/gestao/webhooks").header("Authorization", "Bearer " + gestor))
        .andExpect(jsonPath("$[0].ativo").value(false));
  }

  @Test
  void urlMalformadaEhRecusada() throws Exception {
    mockMvc
        .perform(cadastrar(cidadaos.tokenDeNovoGestor(), "nao-e-uma-url"))
        .andExpect(status().isUnprocessableContent());
  }

  @Test
  void cidadaoNaoAdministraWebhooks() throws Exception {
    String token = cidadaos.token(cidadaos.cadastrar("Cidadão Comum"));

    mockMvc
        .perform(cadastrar(token, "https://93.184.216.34/eventos"))
        .andExpect(status().isForbidden());
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder cadastrar(
      String token, String url) {
    return post("/api/v1/gestao/webhooks")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"url\":\"%s\"}".formatted(url));
  }
}

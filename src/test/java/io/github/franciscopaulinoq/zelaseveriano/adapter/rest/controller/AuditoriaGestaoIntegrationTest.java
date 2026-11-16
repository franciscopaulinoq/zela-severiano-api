package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AuditoriaGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.AuditoriaGestaoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class AuditoriaGestaoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private AuditoriaGestaoService auditoriaService;
  @Autowired private CidadaosDeTeste cidadaos;

  private String tokenGestor;
  private String tokenCidadao;

  @BeforeEach
  void setUp() {
    tokenGestor = cidadaos.tokenDeNovoGestor();
    tokenCidadao = cidadaos.token(cidadaos.cadastrar("Cidadão sem gestão"));
  }

  @Test
  void criacaoRegistraGestorMetodoCaminhoMomentoEStatusUmaVez() throws Exception {
    int antes = auditoriaService.consultar(null, null).size();

    criarCategoria("Auditoria sucesso").andExpect(status().isCreated());

    List<AuditoriaGestaoDTO> eventos = auditoriaService.consultar(null, null);
    assertThat(eventos).hasSize(antes + 1);
    AuditoriaGestaoDTO evento = eventos.getFirst();
    assertThat(evento.gestorId()).isNotNull();
    assertThat(evento.metodo()).isEqualTo("POST");
    assertThat(evento.caminho()).isEqualTo("/api/v1/gestao/categorias");
    assertThat(evento.momento()).isNotNull();
    assertThat(evento.status()).isEqualTo(201);
  }

  @Test
  void erroDeValidacaoTambemFicaRegistrado() throws Exception {
    criarCategoria(" ").andExpect(status().isBadRequest());

    AuditoriaGestaoDTO evento = auditoriaService.consultar(null, "POST").getFirst();
    assertThat(evento.status()).isEqualTo(400);
    assertThat(evento.caminho()).isEqualTo("/api/v1/gestao/categorias");
  }

  @Test
  void consultasGetNaoGeramEventos() throws Exception {
    int antes = auditoriaService.consultar(null, null).size();

    mockMvc
        .perform(get("/api/v1/gestao/relatos").header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/v1/gestao/auditoria").header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk());

    assertThat(auditoriaService.consultar(null, null)).hasSize(antes);
  }

  @Test
  void filtrosPorGestorEMetodoEMaisRecentesPrimeiro() throws Exception {
    String corpo =
        criarCategoria("Auditoria filtros").andReturn().getResponse().getContentAsString();
    Integer categoriaId = JsonPath.read(corpo, "$.id");
    UUID gestorId = auditoriaService.consultar(null, null).getFirst().gestorId();

    mockMvc
        .perform(
            put("/api/v1/gestao/categorias/{id}", categoriaId)
                .header("Authorization", bearer(tokenGestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Auditoria atualizada\",\"iconeNome\":\"edit\"}"))
        .andExpect(status().isOk());

    String resposta =
        mockMvc
            .perform(
                get("/api/v1/gestao/auditoria")
                    .param("gestorId", gestorId.toString())
                    .param("metodo", "post")
                    .header("Authorization", bearer(tokenGestor)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].metodo").value("POST"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(JsonPath.<String>read(resposta, "$[0].gestorId")).isEqualTo(gestorId.toString());

    List<AuditoriaGestaoDTO> doGestor = auditoriaService.consultar(gestorId, null);
    assertThat(doGestor).extracting("metodo").containsExactly("PUT", "POST");
    assertThat(doGestor.getFirst().id()).isGreaterThan(doGestor.get(1).id());
  }

  @Test
  void cidadaoNaoConsultaTrilha() throws Exception {
    mockMvc
        .perform(get("/api/v1/gestao/auditoria").header("Authorization", bearer(tokenCidadao)))
        .andExpect(status().isForbidden());
  }

  private org.springframework.test.web.servlet.ResultActions criarCategoria(String nome)
      throws Exception {
    return mockMvc.perform(
        post("/api/v1/gestao/categorias")
            .header("Authorization", bearer(tokenGestor))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"%s\",\"iconeNome\":\"icone\"}".formatted(nome)));
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class VistoriaAgendamentoIntegrationTest {
  private static final String INICIO = "2026-06-10T10:00:00-03:00";
  private static final String FIM = "2026-06-10T11:00:00-03:00";

  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CidadaosDeTeste cidadaos;

  private Cidadao cidadao;
  private Long relatoId;
  private String tokenGestor;
  private String tokenCidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Autor da Vistoria");
    relatoId =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Relato para vistoria", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    tokenGestor = cidadaos.tokenDeNovoGestor();
    tokenCidadao = cidadaos.token(cidadao);
  }

  @Test
  void vistoriaAgendadaFicaDisponivelParaGestaoEAutor() throws Exception {
    mockMvc
        .perform(agendar(relatoId, INICIO, FIM, tokenGestor))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.relatoId").value(relatoId))
        .andExpect(jsonPath("$.gestorId").isNotEmpty())
        .andExpect(jsonPath("$.status").value("ATIVA"));

    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/{id}/vistorias", relatoId)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
    mockMvc
        .perform(
            get("/api/v1/relatos/{id}/vistorias", relatoId)
                .header("Authorization", bearer(tokenCidadao)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].inicio").value("2026-06-10T13:00:00Z"));
  }

  @Test
  void intervalosInvalidosSaoRecusados() throws Exception {
    for (String[] intervalo : new String[][] {{INICIO, INICIO}, {FIM, INICIO}}) {
      mockMvc
          .perform(agendar(relatoId, intervalo[0], intervalo[1], tokenGestor))
          .andExpect(status().isUnprocessableEntity());
    }
    assertThat(totalVistorias()).isZero();
  }

  @Test
  void conflitoEhInformadoEIntervalosAdjacentesSaoAceitos() throws Exception {
    mockMvc.perform(agendar(relatoId, INICIO, FIM, tokenGestor)).andExpect(status().isCreated());

    mockMvc
        .perform(
            agendar(
                relatoId, "2026-06-10T10:30:00-03:00", "2026-06-10T11:30:00-03:00", tokenGestor))
        .andExpect(status().isConflict());
    mockMvc
        .perform(agendar(relatoId, FIM, "2026-06-10T12:00:00-03:00", tokenGestor))
        .andExpect(status().isCreated());
    assertThat(totalVistorias()).isEqualTo(2);
  }

  @Test
  void vistoriaCanceladaNaoImpedeNovoAgendamento() throws Exception {
    String corpo =
        mockMvc
            .perform(agendar(relatoId, INICIO, FIM, tokenGestor))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Number vistoriaId = JsonPath.read(corpo, "$.id");

    mockMvc
        .perform(
            patch("/api/v1/gestao/vistorias/{id}/cancelamento", vistoriaId)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isNoContent());
    mockMvc.perform(agendar(relatoId, INICIO, FIM, tokenGestor)).andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/{id}/vistorias", relatoId)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].status", containsInAnyOrder("ATIVA", "CANCELADA")));
  }

  @Test
  void autorDeOutroRelatoNaoConsultaVistoriasECidadaoNaoAgenda() throws Exception {
    mockMvc.perform(agendar(relatoId, INICIO, FIM, tokenGestor)).andExpect(status().isCreated());
    String outroToken = cidadaos.token(cidadaos.cadastrar("Outro Cidadão"));

    mockMvc
        .perform(
            get("/api/v1/relatos/{id}/vistorias", relatoId)
                .header("Authorization", bearer(outroToken)))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(agendar(relatoId, FIM, "2026-06-10T12:00:00-03:00", tokenCidadao))
        .andExpect(status().isForbidden());
  }

  @Test
  void agendamentoExigeRelatoExistente() throws Exception {
    mockMvc.perform(agendar(999999L, INICIO, FIM, tokenGestor)).andExpect(status().isNotFound());
  }

  private Integer totalVistorias() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM vistorias WHERE relato_id = ?", Integer.class, relatoId);
  }

  private static MockHttpServletRequestBuilder agendar(
      Long relatoId, String inicio, String fim, String token) {
    String corpo = "{\"inicio\": \"%s\", \"fim\": \"%s\"}".formatted(inicio, fim);
    return post("/api/v1/gestao/relatos/{id}/vistorias", relatoId)
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(corpo);
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}

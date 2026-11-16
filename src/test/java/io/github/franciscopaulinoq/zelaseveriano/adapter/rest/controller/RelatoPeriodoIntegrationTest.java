package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoPeriodoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoSpyBean private RelatoService relatoService;

  private Cidadao cidadao;
  private String tokenGestor;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Ana Souza");
    tokenGestor = cidadaos.tokenDeNovoGestor();
  }

  @Test
  void filtraSomentePelaDataInicial() throws Exception {
    Long anterior = registrarEm(1, "2026-01-01T12:00:00-03:00");
    Long noLimite = registrarEm(1, "2026-01-02T12:00:00-03:00");
    Long posterior = registrarEm(1, "2026-01-03T12:00:00-03:00");

    mockMvc
        .perform(listar().param("dataInicial", "2026-01-02").param("tamanho", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", noLimite).exists())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", posterior).exists())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", anterior).doesNotExist());
  }

  @Test
  void incluiExtremidadesETambemFiltraSomentePelaDataFinal() throws Exception {
    Long primeiroDia = registrarEm(1, "2026-02-01T12:00:00-03:00");
    Long segundoDia = registrarEm(1, "2026-02-02T12:00:00-03:00");
    Long foraDoPeriodo = registrarEm(1, "2026-02-03T12:00:00-03:00");

    mockMvc
        .perform(listar().param("dataInicial", "2026-02-01").param("dataFinal", "2026-02-02"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.content[*].id",
                containsInAnyOrder(primeiroDia.intValue(), segundoDia.intValue())));
    mockMvc
        .perform(listar().param("dataFinal", "2026-02-02"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", primeiroDia).exists())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", segundoDia).exists())
        .andExpect(jsonPath("$.content[?(@.id == %d)]", foraDoPeriodo).doesNotExist());
  }

  @Test
  void combinaPeriodoStatusCategoriaEPaginacao() throws Exception {
    Long esperado = registrarEm(1, "2026-03-10T12:00:00-03:00");
    Long emAnalise = registrarEm(1, "2026-03-10T13:00:00-03:00");
    relatoService.atualizarStatus(emAnalise, StatusRelato.EM_ANALISE, null);
    registrarEm(3, "2026-03-10T14:00:00-03:00");
    registrarEm(1, "2026-03-11T12:00:00-03:00");

    mockMvc
        .perform(
            listar()
                .param("dataInicial", "2026-03-10")
                .param("dataFinal", "2026-03-10")
                .param("status", "PENDENTE")
                .param("categoriaId", "1")
                .param("tamanho", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(esperado))
        .andExpect(jsonPath("$.page.totalElements").value(1))
        .andExpect(jsonPath("$.page.totalPages").value(1));
  }

  @Test
  void intervaloInvertidoNaoExecutaConsulta() throws Exception {
    mockMvc
        .perform(listar().param("dataInicial", "2026-04-03").param("dataFinal", "2026-04-02"))
        .andExpect(status().isUnprocessableEntity());

    verify(relatoService, never()).listarTodos(any(), any());
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder listar() {
    return get("/api/v1/gestao/relatos").header("Authorization", "Bearer " + tokenGestor);
  }

  private Long registrarEm(int categoriaId, String criadoEm) {
    Long id =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(categoriaId, "Relato de teste", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    jdbcTemplate.update(
        "UPDATE relatos SET criado_em = ? WHERE id = ?", OffsetDateTime.parse(criadoEm), id);
    return id;
  }
}

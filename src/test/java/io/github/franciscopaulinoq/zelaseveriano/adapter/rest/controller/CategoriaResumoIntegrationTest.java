package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class CategoriaResumoIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CategoriaService categoriaService;

  @Autowired private RelatoService relatoService;

  @Autowired private CidadaosDeTeste cidadaos;

  private String tokenGestor;
  private Cidadao cidadao;
  private Integer categoriaId;

  @BeforeEach
  void setUp() {
    tokenGestor = cidadaos.tokenDeNovoGestor();
    cidadao = cidadaos.cadastrar("Maria Souza");
    categoriaId =
        categoriaService
            .criar(new CategoriaRegistroDTO("Resumo " + UUID.randomUUID(), "resumo"))
            .getId();
  }

  @Test
  void resumoContaSomenteRelatosPendentes() throws Exception {
    registrar();
    registrar();
    registrar();
    Long concluido = registrar();
    relatoService.atualizarStatus(concluido, StatusRelato.EM_ANALISE, null);
    relatoService.atualizarStatus(concluido, StatusRelato.CONCLUIDO, "Resolvido");
    Long cancelado = registrar();
    relatoService.cancelar(cidadao.usuarioId(), cancelado);

    mockMvc
        .perform(
            get("/api/v1/gestao/categorias/{id}/resumo", categoriaId)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(categoriaId))
        .andExpect(jsonPath("$.nome").value(org.hamcrest.Matchers.startsWith("Resumo ")))
        .andExpect(jsonPath("$.totalPendentes").value(3))
        .andExpect(jsonPath("$.possuiPendencias").value(true));
  }

  @Test
  void resumoSemPendenciasIndicaZero() throws Exception {
    Long concluido = registrar();
    relatoService.atualizarStatus(concluido, StatusRelato.EM_ANALISE, null);
    relatoService.atualizarStatus(concluido, StatusRelato.CONCLUIDO, "Resolvido");

    mockMvc
        .perform(
            get("/api/v1/gestao/categorias/{id}/resumo", categoriaId)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPendentes").value(0))
        .andExpect(jsonPath("$.possuiPendencias").value(false));
  }

  @Test
  void categoriaInexistenteNaoPossuiResumo() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/categorias/{id}/resumo", 999999)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isNotFound());
  }

  @Test
  void cidadaoNaoConsultaResumoDeGestao() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/categorias/{id}/resumo", categoriaId)
                .header("Authorization", bearer(cidadaos.token(cidadao))))
        .andExpect(status().isForbidden());
  }

  private Long registrar() {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(categoriaId, "Relato de teste", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}

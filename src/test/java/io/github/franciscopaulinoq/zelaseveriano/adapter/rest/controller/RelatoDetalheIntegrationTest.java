package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoDetalheIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;

  private Cidadao cidadao;
  private String token;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Maria da Silva");
    token = cidadaos.token(cidadao);
  }

  @Test
  void detalheApresentaJustificativaDaRejeicao() throws Exception {
    Long relatoId = registrar();
    relatoService.atualizarStatus(relatoId, StatusRelato.REJEITADO, "Foto insuficiente");

    mockMvc
        .perform(get("/api/v1/relatos/{id}", relatoId).header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.observacaoResolucao").value("Foto insuficiente"))
        .andExpect(jsonPath("$.atualizadoEm").isNotEmpty())
        .andExpect(jsonPath("$.descricao").value("Poste apagado"));
  }

  @Test
  void detalheApresentaObservacaoDaUltimaAvaliacao() throws Exception {
    Long relatoId = registrar();
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, "Equipe designada");
    relatoService.atualizarStatus(relatoId, StatusRelato.CONCLUIDO, "Luminária substituída");

    mockMvc
        .perform(get("/api/v1/relatos/{id}", relatoId).header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.observacaoResolucao").value("Luminária substituída"))
        .andExpect(jsonPath("$.atualizadoEm").isNotEmpty());
  }

  @Test
  void detalheDeOutroCidadaoContinuaInacessivel() throws Exception {
    Long relatoId = registrar();
    String outroToken = cidadaos.token(cidadaos.cadastrar("Outro Cidadão"));

    mockMvc
        .perform(get("/api/v1/relatos/{id}", relatoId).header("Authorization", bearer(outroToken)))
        .andExpect(status().isNotFound());
  }

  private Long registrar() {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(2, "Poste apagado", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}

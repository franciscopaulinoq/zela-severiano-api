package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoReaberturaIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;

  private Cidadao cidadao;
  private String token;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Rita Gomes");
    token = cidadaos.token(cidadao);
  }

  @Test
  void cidadaoReabreRelatoRejeitadoEPodeAvalialoNovamente() throws Exception {
    Long relatoId = relatoRejeitado();

    mockMvc.perform(reabrir(relatoId, token)).andExpect(status().isNoContent());

    assertThat(relatoService.obterDetalhe(cidadao.usuarioId(), relatoId).getStatus())
        .isEqualTo(StatusRelato.PENDENTE);
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId))
        .hasSize(2)
        .last()
        .satisfies(historico -> {
          assertThat(historico.getStatusAnterior()).isEqualTo(StatusRelato.REJEITADO);
          assertThat(historico.getStatusNovo()).isEqualTo(StatusRelato.PENDENTE);
        });

    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, "Nova análise");
    assertThat(relatoService.obterDetalhe(cidadao.usuarioId(), relatoId).getStatus())
        .isEqualTo(StatusRelato.EM_ANALISE);
  }

  @Test
  void relatosEmOutrosStatusSaoRecusadosSemNovoHistorico() throws Exception {
    Long emAnalise = registrar();
    relatoService.atualizarStatus(emAnalise, StatusRelato.EM_ANALISE, null);
    Long concluido = registrar();
    relatoService.atualizarStatus(concluido, StatusRelato.EM_ANALISE, null);
    relatoService.atualizarStatus(concluido, StatusRelato.CONCLUIDO, "Resolvido");
    Long cancelado = registrar();
    relatoService.cancelar(cidadao.usuarioId(), cancelado);
    List<Long> relatos = List.of(emAnalise, concluido, cancelado);

    for (Long relatoId : relatos) {
      mockMvc.perform(reabrir(relatoId, token)).andExpect(status().isConflict());
    }

    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), emAnalise)).hasSize(1);
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), concluido)).hasSize(2);
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), cancelado)).hasSize(1);
  }

  @Test
  void cidadaoNaoReabreRelatoDeOutroAutor() throws Exception {
    Long relatoId = relatoRejeitado();
    String outroToken = cidadaos.token(cidadaos.cadastrar("Outro Cidadão"));

    mockMvc.perform(reabrir(relatoId, outroToken)).andExpect(status().isNotFound());

    assertThat(relatoService.obterDetalhe(cidadao.usuarioId(), relatoId).getStatus())
        .isEqualTo(StatusRelato.REJEITADO);
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId)).hasSize(1);
  }

  private Long relatoRejeitado() {
    Long relatoId = registrar();
    relatoService.atualizarStatus(relatoId, StatusRelato.REJEITADO, "Informações insuficientes");
    return relatoId;
  }

  private Long registrar() {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(2, "Vazamento", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private static MockHttpServletRequestBuilder reabrir(Long relatoId, String token) {
    return patch("/api/v1/relatos/{id}/reabertura", relatoId)
        .header("Authorization", "Bearer " + token);
  }
}

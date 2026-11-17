package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.notification.EmailSender;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class NotificacaoRelatoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoBean private EmailSender emailSender;

  private Cidadao cidadao;
  private Long relatoId;
  private String tokenGestor;
  private String tokenCidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Autor da Notificação");
    relatoId =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Relato para notificar", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    tokenGestor = cidadaos.tokenDeNovoGestor();
    tokenCidadao = cidadaos.token(cidadao);
  }

  @Test
  void mudancaPelaGestaoEnviaEmailSemAlterarHistorico() throws Exception {
    mockMvc.perform(atualizarStatus("EM_ANALISE")).andExpect(status().isNoContent());

    verificarEmail("EM_ANALISE");
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId))
        .singleElement()
        .satisfies(historico -> {
          assertThat(historico.getStatusAnterior()).isEqualTo(StatusRelato.PENDENTE);
          assertThat(historico.getStatusNovo()).isEqualTo(StatusRelato.EM_ANALISE);
          assertThat(historico.getObservacao()).isEqualTo("Equipe designada");
        });
  }

  @Test
  void cancelamentoPeloCidadaoEnviaConfirmacao() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/relatos/{id}/cancelamento", relatoId)
                .header("Authorization", "Bearer " + tokenCidadao))
        .andExpect(status().isNoContent());

    verificarEmail("CANCELADO");
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId)).hasSize(1);
  }

  @Test
  void perfilSemEmailMantemMudancaSemEnvio() throws Exception {
    jdbcTemplate.update("UPDATE perfis SET email = NULL WHERE id = ?", cidadao.perfilId());

    mockMvc.perform(atualizarStatus("EM_ANALISE")).andExpect(status().isNoContent());

    verifyNoInteractions(emailSender);
  }

  @Test
  void transicaoInvalidaNaoEnviaEmail() throws Exception {
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);
    relatoService.atualizarStatus(relatoId, StatusRelato.CONCLUIDO, "Resolvido");
    clearInvocations(emailSender);

    mockMvc.perform(atualizarStatus("EM_ANALISE")).andExpect(status().isConflict());

    verifyNoInteractions(emailSender);
  }

  @Test
  void mudancasSucessivasEnviamUmEmailPorStatusSemDuplicarHistorico() throws Exception {
    mockMvc.perform(atualizarStatus("EM_ANALISE")).andExpect(status().isNoContent());
    mockMvc.perform(atualizarStatus("CONCLUIDO")).andExpect(status().isNoContent());

    verificarEmail("EM_ANALISE");
    verificarEmail("CONCLUIDO");
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId)).hasSize(2);
  }

  @Test
  void emailEmBrancoNaoGeraTentativaDeEnvio() throws Exception {
    jdbcTemplate.update("UPDATE perfis SET email = '  ' WHERE id = ?", cidadao.perfilId());

    mockMvc.perform(atualizarStatus("EM_ANALISE")).andExpect(status().isNoContent());

    verifyNoInteractions(emailSender);
    assertThat(relatoService.obterHistorico(cidadao.usuarioId(), relatoId)).hasSize(1);
  }

  @Test
  void cancelamentoDeOutroAutorNaoEnviaEmail() throws Exception {
    String outroToken = cidadaos.token(cidadaos.cadastrar("Outro Cidadão"));

    mockMvc
        .perform(
            patch("/api/v1/relatos/{id}/cancelamento", relatoId)
                .header("Authorization", "Bearer " + outroToken))
        .andExpect(status().isNotFound());

    verifyNoInteractions(emailSender);
  }

  @Test
  void cancelamentoInvalidoNaoEnviaEmail() throws Exception {
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);
    clearInvocations(emailSender);

    mockMvc
        .perform(
            patch("/api/v1/relatos/{id}/cancelamento", relatoId)
                .header("Authorization", "Bearer " + tokenCidadao))
        .andExpect(status().isConflict());

    verifyNoInteractions(emailSender);
  }

  @Test
  void relatoInexistenteNaoEnviaEmail() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/gestao/relatos/999999/status")
                .header("Authorization", "Bearer " + tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"EM_ANALISE\"}"))
        .andExpect(status().isNotFound());

    verifyNoInteractions(emailSender);
  }

  private void verificarEmail(String status) {
    verify(emailSender)
        .enviar(
            eq("cidadao@example.com"),
            eq("Atualização do relato #" + relatoId),
            argThat(corpo -> corpo.contains(String.valueOf(relatoId)) && corpo.contains(status)));
  }

  private MockHttpServletRequestBuilder atualizarStatus(String status) {
    return patch("/api/v1/gestao/relatos/{id}/status", relatoId)
        .header("Authorization", "Bearer " + tokenGestor)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"status\": \"%s\", \"observacao\": \"Equipe designada\"}".formatted(status));
  }
}

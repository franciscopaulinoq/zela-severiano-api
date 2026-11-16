package io.github.franciscopaulinoq.zelaseveriano.infrastructure.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.WebhookDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.WebhookService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EntregaWebhookEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository.JpaEntregaWebhookRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class WebhookDispatcherIntegrationTest {
  private static final Instant INICIO = Instant.parse("2026-01-10T12:00:00Z");

  @Autowired private WebhookService webhookService;
  @Autowired private WebhookDispatcher dispatcher;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private JpaEntregaWebhookRepository entregaRepository;
  @MockitoBean private WebhookHttpClient httpClient;
  @MockitoBean private Clock clock;

  private Cidadao cidadao;
  private Long relatoId;
  private WebhookDTO webhook;

  @BeforeEach
  void setUp() {
    webhookService.listar().forEach(destino -> webhookService.inativar(destino.id()));
    entregaRepository.deleteAll();
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(clock.instant()).thenReturn(INICIO);
    cidadao = cidadaos.cadastrar("Autor do Relato");
    relatoId =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Poste apagado", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    webhook = webhookService.cadastrar("https://93.184.216.34/eventos");
  }

  @Test
  void eventoCriaEntregaComDadosEConfirmaSucesso() {
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);

    dispatcher.processar();

    ArgumentCaptor<EntregaWebhook> captor = ArgumentCaptor.forClass(EntregaWebhook.class);
    verify(httpClient).enviar(captor.capture(), org.mockito.ArgumentMatchers.eq(webhook.url()));
    assertThat(captor.getValue().getRelatoId()).isEqualTo(relatoId);
    assertThat(captor.getValue().getStatusAnterior()).isEqualTo(StatusRelato.PENDENTE);
    assertThat(captor.getValue().getStatusNovo()).isEqualTo(StatusRelato.EM_ANALISE);
    assertThat(captor.getValue().getId()).isNotNull();
    assertThat(entregaRepository.findAll().getFirst().getEntregueEm()).isNotNull();
  }

  @Test
  void falhaPermanecePendenteEeReprocessada() {
    doThrow(new IllegalStateException("indisponível")).when(httpClient).enviar(any(), anyString());
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);

    dispatcher.processar();

    EntregaWebhookEntity pendente = entregaRepository.findAll().getFirst();
    assertThat(pendente.getEntregueEm()).isNull();
    assertThat(pendente.getTentativas()).isEqualTo(1);
    assertThat(pendente.getProximaTentativaEm()).isAfter(pendente.getCriadoEm());

    reset(httpClient);
    when(clock.instant()).thenReturn(INICIO.plus(3, ChronoUnit.SECONDS));
    dispatcher.processar();

    verify(httpClient).enviar(any(), anyString());
    assertThat(entregaRepository.findAll().getFirst().getEntregueEm()).isNotNull();
  }

  @Test
  void webhookInativoNaoRecebeNovosEventos() {
    relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);
    webhookService.inativar(webhook.id());

    dispatcher.processar();

    verifyNoInteractions(httpClient);
    assertThat(entregaRepository.findAll()).isEmpty();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.WebhookDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.WebhookDestinationValidator;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.HistoricoStatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Webhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EntregaWebhookRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.WebhookRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookService {
  private static final int LOTE_EVENTOS = 100;

  private final WebhookRepository webhookRepository;
  private final EntregaWebhookRepository entregaRepository;
  private final HistoricoStatusRelatoRepository historicoRepository;
  private final WebhookDestinationValidator destinationValidator;
  private final Clock clock;

  @Transactional
  public WebhookDTO cadastrar(String url) {
    destinationValidator.validar(url);
    Webhook webhook =
        Webhook.builder()
            .id(UUID.randomUUID())
            .url(url)
            .ativo(true)
            .criadoEm(OffsetDateTime.now(clock))
            .ultimoEventoId(historicoRepository.maxId())
            .build();
    return toDTO(webhookRepository.save(webhook));
  }

  @Transactional(readOnly = true)
  public List<WebhookDTO> listar() {
    return webhookRepository.findAll().stream().map(this::toDTO).toList();
  }

  @Transactional
  public void inativar(UUID id) {
    Webhook webhook = buscar(id);
    webhook.inativar();
    webhookRepository.save(webhook);
  }

  @Transactional
  public void sincronizar(UUID id) {
    Webhook webhook =
        webhookRepository
            .findByIdParaAtualizacao(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Webhook não encontrado."));
    if (!webhook.isAtivo()) {
      return;
    }

    List<HistoricoStatusRelato> eventos =
        historicoRepository.findAfterId(webhook.getUltimoEventoId(), LOTE_EVENTOS);
    if (eventos.isEmpty()) {
      return;
    }
    OffsetDateTime agora = OffsetDateTime.now(clock);
    entregaRepository.saveAll(
        eventos.stream().map(evento -> novaEntrega(webhook, evento, agora)).toList());
    webhook.avancarCursor(eventos.getLast().getId());
    webhookRepository.save(webhook);
  }

  private EntregaWebhook novaEntrega(
      Webhook webhook, HistoricoStatusRelato evento, OffsetDateTime agora) {
    return EntregaWebhook.builder()
        .id(UUID.randomUUID())
        .webhookId(webhook.getId())
        .historicoId(evento.getId())
        .relatoId(evento.getRelatoId())
        .statusAnterior(evento.getStatusAnterior())
        .statusNovo(evento.getStatusNovo())
        .momentoMudanca(evento.getAlteradoEm())
        .criadoEm(agora)
        .proximaTentativaEm(agora)
        .build();
  }

  private Webhook buscar(UUID id) {
    return webhookRepository
        .findByIdParaAtualizacao(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Webhook não encontrado."));
  }

  private WebhookDTO toDTO(Webhook webhook) {
    return new WebhookDTO(
        webhook.getId(), webhook.getUrl(), webhook.isAtivo(), webhook.getCriadoEm());
  }
}

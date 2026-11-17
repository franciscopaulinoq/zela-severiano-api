package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.domain.event.MudancaStatusRelatoEvent;
import io.github.franciscopaulinoq.zelaseveriano.domain.notification.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacaoRelatoService {
  private final EmailSender emailSender;

  @EventListener
  public void notificarMudanca(MudancaStatusRelatoEvent evento) {
    if (evento.email() == null || evento.email().isBlank()) {
      return;
    }
    emailSender.enviar(
        evento.email(),
        "Atualização do relato #" + evento.relatoId(),
        "O novo status do relato #%d é %s.".formatted(evento.relatoId(), evento.novoStatus()));
  }
}

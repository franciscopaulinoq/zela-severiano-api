package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.franciscopaulinoq.zelaseveriano.domain.event.MudancaStatusRelatoEvent;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.notification.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacaoRelatoServiceTest {
  @Mock private EmailSender emailSender;
  @InjectMocks private NotificacaoRelatoService notificacaoService;

  @Test
  void notificaEmailComIdENovoStatusDoRelato() {
    MudancaStatusRelatoEvent evento =
        new MudancaStatusRelatoEvent(42L, "autor@example.com", StatusRelato.EM_ANALISE);

    notificacaoService.notificarMudanca(evento);

    verify(emailSender)
        .enviar(
            eq("autor@example.com"),
            eq("Atualização do relato #42"),
            argThat(corpo -> corpo.contains("42") && corpo.contains("EM_ANALISE")));
  }

  @ParameterizedTest
  @EnumSource(StatusRelato.class)
  void mensagemApresentaQualquerNovoStatus(StatusRelato status) {
    notificacaoService.notificarMudanca(
        new MudancaStatusRelatoEvent(99L, "autor@example.com", status));

    verify(emailSender)
        .enviar(
            eq("autor@example.com"),
            eq("Atualização do relato #99"),
            argThat(corpo -> corpo.contains(status.name())));
  }

  @Test
  void perfilSemEmailNaoGeraEnvio() {
    notificacaoService.notificarMudanca(
        new MudancaStatusRelatoEvent(42L, null, StatusRelato.CONCLUIDO));
    notificacaoService.notificarMudanca(
        new MudancaStatusRelatoEvent(43L, "  ", StatusRelato.CANCELADO));

    verifyNoInteractions(emailSender);
  }
}

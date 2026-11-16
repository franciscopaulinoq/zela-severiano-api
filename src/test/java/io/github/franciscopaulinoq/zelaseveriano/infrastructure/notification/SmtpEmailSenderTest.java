package io.github.franciscopaulinoq.zelaseveriano.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {
  @Mock private JavaMailSender javaMailSender;
  @InjectMocks private SmtpEmailSender emailSender;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailSender, "remetente", "no-reply@example.com");
  }

  @Test
  void montaMensagemSmtpComRemetenteDestinatarioAssuntoEConteudo() {
    emailSender.enviar("autor@example.com", "Atualização do relato #42", "Novo status");

    ArgumentCaptor<SimpleMailMessage> mensagem = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(javaMailSender).send(mensagem.capture());
    assertThat(mensagem.getValue().getFrom()).isEqualTo("no-reply@example.com");
    assertThat(mensagem.getValue().getTo()).containsExactly("autor@example.com");
    assertThat(mensagem.getValue().getSubject()).isEqualTo("Atualização do relato #42");
    assertThat(mensagem.getValue().getText()).isEqualTo("Novo status");
  }
}

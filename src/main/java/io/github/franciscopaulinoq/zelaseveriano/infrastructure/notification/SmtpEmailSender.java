package io.github.franciscopaulinoq.zelaseveriano.infrastructure.notification;

import io.github.franciscopaulinoq.zelaseveriano.domain.notification.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {
  private final JavaMailSender javaMailSender;

  @Value("${api.notificacoes.email.remetente}")
  private String remetente;

  @Override
  public void enviar(String destinatario, String assunto, String mensagem) {
    SimpleMailMessage email = new SimpleMailMessage();
    email.setFrom(remetente);
    email.setTo(destinatario);
    email.setSubject(assunto);
    email.setText(mensagem);
    javaMailSender.send(email);
  }
}

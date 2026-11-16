package io.github.franciscopaulinoq.zelaseveriano.domain.notification;

public interface EmailSender {
  void enviar(String destinatario, String assunto, String mensagem);
}

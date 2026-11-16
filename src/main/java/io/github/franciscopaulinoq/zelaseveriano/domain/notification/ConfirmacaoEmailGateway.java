package io.github.franciscopaulinoq.zelaseveriano.domain.notification;

public interface ConfirmacaoEmailGateway {
  void enviar(String email, String codigo);
}

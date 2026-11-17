package io.github.franciscopaulinoq.zelaseveriano.domain.security;

public interface RecuperacaoSenhaEmailGateway {
  void enviar(String email, String codigo);
}

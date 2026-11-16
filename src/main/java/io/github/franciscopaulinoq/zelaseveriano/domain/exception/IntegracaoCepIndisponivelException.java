package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class IntegracaoCepIndisponivelException extends RuntimeException {
  public IntegracaoCepIndisponivelException(Throwable cause) {
    super(
        "Consulta de CEP indisponível. Tente novamente ou preencha o endereço manualmente.", cause);
  }
}

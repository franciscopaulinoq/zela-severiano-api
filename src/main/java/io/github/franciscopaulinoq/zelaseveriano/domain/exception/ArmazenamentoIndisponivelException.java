package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class ArmazenamentoIndisponivelException extends RuntimeException {
  public ArmazenamentoIndisponivelException(String message, Throwable cause) {
    super(message, cause);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class ConflitoVistoriaException extends RuntimeException {
  public ConflitoVistoriaException() {
    super("O gestor já possui uma vistoria ativa nesse horário.");
  }
}

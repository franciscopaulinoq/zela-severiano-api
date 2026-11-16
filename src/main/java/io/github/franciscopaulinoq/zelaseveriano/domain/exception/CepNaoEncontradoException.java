package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class CepNaoEncontradoException extends RecursoNaoEncontradoException {
  public CepNaoEncontradoException() {
    super("CEP não encontrado. Continue o preenchimento manual do endereço.");
  }
}

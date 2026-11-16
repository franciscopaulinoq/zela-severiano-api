package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class CategoriaJaRegistradaException extends RuntimeException {
  public CategoriaJaRegistradaException() {
    super("Já existe uma categoria com esse nome.");
  }
}

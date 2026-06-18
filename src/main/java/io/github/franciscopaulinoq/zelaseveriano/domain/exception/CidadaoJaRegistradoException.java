package io.github.franciscopaulinoq.zelaseveriano.domain.exception;

public class CidadaoJaRegistradoException extends RuntimeException {

    public CidadaoJaRegistradoException(String message) {
        super(message);
    }

    public CidadaoJaRegistradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
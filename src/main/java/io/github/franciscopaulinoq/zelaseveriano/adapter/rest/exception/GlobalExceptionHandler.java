package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.exception;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Um ou mais campos contêm dados inválidos.")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(CidadaoJaRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleCidadaoJaRegistrado(
            CidadaoJaRegistradoException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflito de regra de negócio")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleTransicaoStatusInvalida(
            TransicaoStatusInvalidaException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Transição de status inválida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Entidade não processável")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Erro interno do servidor")
                .message("Ocorreu um erro inesperado no sistema. Por favor, tente novamente mais tarde.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

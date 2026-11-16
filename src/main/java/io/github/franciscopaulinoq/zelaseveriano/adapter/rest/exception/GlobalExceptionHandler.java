package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.exception;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaEmUsoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaJaRegistradaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CredenciaisInvalidasException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.LimiteRelatosPendentesException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TokenInvalidoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(error -> {
          String fieldName =
              error instanceof FieldError fieldError
                  ? fieldError.getField()
                  : error.getObjectName();
          errors.put(fieldName, error.getDefaultMessage());
        });
    return resposta(
        HttpStatus.BAD_REQUEST,
        "Erro de validação",
        "Um ou mais campos contêm dados inválidos.",
        request,
        errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTipoDeParametroInvalido(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return resposta(
        HttpStatus.BAD_REQUEST,
        "Parâmetro inválido",
        "Valor inválido para o parâmetro '%s'.".formatted(ex.getName()),
        request,
        null);
  }

  @ExceptionHandler(CidadaoJaRegistradoException.class)
  public ResponseEntity<ErrorResponse> handleCidadaoJaRegistrado(
      CidadaoJaRegistradoException ex, HttpServletRequest request) {
    return resposta(
        HttpStatus.CONFLICT, "Conflito de regra de negócio", ex.getMessage(), request, null);
  }

  @ExceptionHandler(CategoriaJaRegistradaException.class)
  public ResponseEntity<ErrorResponse> handleCategoriaJaRegistrada(
      CategoriaJaRegistradaException ex, HttpServletRequest request) {
    return resposta(HttpStatus.CONFLICT, "Categoria já cadastrada", ex.getMessage(), request, null);
  }

  @ExceptionHandler(CategoriaEmUsoException.class)
  public ResponseEntity<ErrorResponse> handleCategoriaEmUso(
      CategoriaEmUsoException ex, HttpServletRequest request) {
    return resposta(HttpStatus.CONFLICT, "Categoria em uso", ex.getMessage(), request, null);
  }

  @ExceptionHandler(LimiteRelatosPendentesException.class)
  public ResponseEntity<ErrorResponse> handleLimiteRelatosPendentes(
      LimiteRelatosPendentesException ex, HttpServletRequest request) {
    return resposta(
        HttpStatus.CONFLICT,
        "Limite de relatos pendentes atingido",
        ex.getMessage(),
        request,
        null);
  }

  @ExceptionHandler(CredenciaisInvalidasException.class)
  public ResponseEntity<ErrorResponse> handleNaoAutorizado(
      RuntimeException ex, HttpServletRequest request) {
    return resposta(HttpStatus.UNAUTHORIZED, "Não autorizado", ex.getMessage(), request, null);
  }

  @ExceptionHandler(TokenInvalidoException.class)
  public ResponseEntity<ErrorResponse> handleTokenInvalido(
      TokenInvalidoException ex, HttpServletRequest request) {
    return handleNaoAutorizado(ex, request);
  }

  @ExceptionHandler(TransicaoStatusInvalidaException.class)
  public ResponseEntity<ErrorResponse> handleTransicaoStatusInvalida(
      TransicaoStatusInvalidaException ex, HttpServletRequest request) {
    return resposta(
        HttpStatus.CONFLICT, "Transição de status inválida", ex.getMessage(), request, null);
  }

  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(
      RecursoNaoEncontradoException ex, HttpServletRequest request) {
    return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request, null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return resposta(
        HttpStatus.UNPROCESSABLE_CONTENT,
        "Entidade não processável",
        ex.getMessage(),
        request,
        null);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestPartException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ErrorResponse> handleRequisicaoMalformada(HttpServletRequest request) {
    return resposta(
        HttpStatus.BAD_REQUEST,
        "Requisição inválida",
        "O corpo ou os parâmetros da requisição estão ausentes ou malformados.",
        request,
        null);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleUploadExcedente(HttpServletRequest request) {
    return resposta(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "Arquivo muito grande",
        "O arquivo enviado excede o limite de 10 MB.",
        request,
        null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
    return resposta(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno do servidor",
        "Ocorreu um erro inesperado no sistema. Por favor, tente novamente mais tarde.",
        request,
        null);
  }

  private ResponseEntity<ErrorResponse> resposta(
      HttpStatus status,
      String error,
      String message,
      HttpServletRequest request,
      Map<String, String> validationErrors) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .status(status.value())
            .error(error)
            .message(message)
            .path(request.getRequestURI())
            .validationErrors(validationErrors)
            .build();
    return ResponseEntity.status(status).body(errorResponse);
  }
}

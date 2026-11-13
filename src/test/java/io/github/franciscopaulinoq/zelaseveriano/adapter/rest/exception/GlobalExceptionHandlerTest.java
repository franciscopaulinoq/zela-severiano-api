package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.exception;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/cidadaos");

    @Test
    void cpfJaRegistradoViraConflito() {
        ResponseEntity<ErrorResponse> resposta =
                handler.handleCidadaoJaRegistrado(new CidadaoJaRegistradoException("CPF já registrado"), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().getMessage()).isEqualTo("CPF já registrado");
        assertThat(resposta.getBody().getPath()).isEqualTo("/api/v1/cidadaos");
    }

    @Test
    void transicaoDeStatusInvalidaViraConflito() {
        ResponseEntity<ErrorResponse> resposta = handler.handleTransicaoStatusInvalida(
                new TransicaoStatusInvalidaException("Transição não permitida"), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().getMessage()).isEqualTo("Transição não permitida");
    }

    @Test
    void argumentoInvalidoViraEntidadeNaoProcessavel() {
        ResponseEntity<ErrorResponse> resposta =
                handler.handleIllegalArgument(new IllegalArgumentException("Categoria não encontrada."), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody().getStatus()).isEqualTo(422);
        assertThat(resposta.getBody().getMessage()).isEqualTo("Categoria não encontrada.");
    }

    @Test
    void erroInesperadoViraErroInternoERegistraODetalheNoLog(CapturedOutput saida) {
        ResponseEntity<ErrorResponse> resposta =
                handler.handleGenericException(new IllegalStateException("conexão recusada"), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resposta.getBody().getMessage()).doesNotContain("conexão recusada");
        assertThat(saida.getOut())
                .contains("ERROR")
                .contains("POST /api/v1/cidadaos")
                .contains("java.lang.IllegalStateException: conexão recusada");
    }
}

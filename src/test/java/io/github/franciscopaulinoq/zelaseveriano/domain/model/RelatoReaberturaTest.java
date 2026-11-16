package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RelatoReaberturaTest {

  @Test
  void relatoRejeitadoVoltaParaPendente() {
    Relato relato =
        Relato.builder()
            .status(StatusRelato.REJEITADO)
            .observacaoResolucao("Informações insuficientes")
            .atualizadoEm(OffsetDateTime.parse("2026-01-01T10:00:00-03:00"))
            .build();

    relato.reabrir();

    assertThat(relato.getStatus()).isEqualTo(StatusRelato.PENDENTE);
    assertThat(relato.getObservacaoResolucao()).isNull();
    assertThat(relato.getAtualizadoEm()).isAfter(OffsetDateTime.parse("2026-01-01T10:00:00-03:00"));
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusRelato.class,
      names = {"PENDENTE", "EM_ANALISE", "CONCLUIDO", "CANCELADO"})
  void outrosStatusNaoPodemSerReabertos(StatusRelato status) {
    Relato relato = Relato.builder().status(status).build();

    assertThatThrownBy(relato::reabrir).isInstanceOf(TransicaoStatusInvalidaException.class);
    assertThat(relato.getStatus()).isEqualTo(status);
    assertThat(relato.getAtualizadoEm()).isNull();
  }
}

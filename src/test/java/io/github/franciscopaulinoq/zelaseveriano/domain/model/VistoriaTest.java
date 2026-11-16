package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VistoriaTest {
  private static final OffsetDateTime INICIO = OffsetDateTime.parse("2026-06-10T10:00:00-03:00");
  private static final OffsetDateTime FIM = INICIO.plusHours(1);

  @Test
  void agendamentoValidoNasceAtivo() {
    UUID gestorId = UUID.randomUUID();

    Vistoria vistoria = Vistoria.agendar(42L, gestorId, INICIO, FIM);

    assertThat(vistoria.getRelatoId()).isEqualTo(42L);
    assertThat(vistoria.getGestorId()).isEqualTo(gestorId);
    assertThat(vistoria.getInicio()).isEqualTo(INICIO);
    assertThat(vistoria.getFim()).isEqualTo(FIM);
    assertThat(vistoria.getStatus()).isEqualTo(StatusVistoria.ATIVA);
  }

  @ParameterizedTest
  @MethodSource("intervalosInvalidos")
  void intervaloInvalidoEhRecusado(OffsetDateTime inicio, OffsetDateTime fim) {
    assertThatThrownBy(() -> Vistoria.agendar(42L, UUID.randomUUID(), inicio, fim))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void cancelamentoAlteraApenasOStatus() {
    Vistoria vistoria = Vistoria.agendar(42L, UUID.randomUUID(), INICIO, FIM);

    vistoria.cancelar();

    assertThat(vistoria.getStatus()).isEqualTo(StatusVistoria.CANCELADA);
    assertThat(vistoria.getInicio()).isEqualTo(INICIO);
    assertThat(vistoria.getFim()).isEqualTo(FIM);
  }

  private static Stream<Arguments> intervalosInvalidos() {
    return Stream.of(
        Arguments.of(INICIO, INICIO),
        Arguments.of(FIM, INICIO),
        Arguments.of(null, FIM),
        Arguments.of(INICIO, null));
  }
}

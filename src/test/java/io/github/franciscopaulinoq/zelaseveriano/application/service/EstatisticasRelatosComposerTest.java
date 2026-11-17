package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO.TotalPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.util.List;
import org.junit.jupiter.api.Test;

class EstatisticasRelatosComposerTest {

  private final EstatisticasRelatosComposer composer = new EstatisticasRelatosComposer();

  @Test
  void compoeTodosOsStatusEMantemCategoriasZeradasEOrdenadas() {
    var estatisticas =
        composer.compor(
            6,
            List.of(
                new TotalRelatosPorStatus(StatusRelato.PENDENTE, 4),
                new TotalRelatosPorStatus(StatusRelato.CONCLUIDO, 2)),
            List.of(
                new TotalRelatosPorCategoria(1, "Iluminação", 6),
                new TotalRelatosPorCategoria(3, "Limpeza", 0)));

    assertThat(estatisticas.total()).isEqualTo(6);
    assertThat(estatisticas.porStatus())
        .containsExactly(
            org.assertj.core.data.MapEntry.entry(StatusRelato.PENDENTE, 4L),
            org.assertj.core.data.MapEntry.entry(StatusRelato.EM_ANALISE, 0L),
            org.assertj.core.data.MapEntry.entry(StatusRelato.CONCLUIDO, 2L),
            org.assertj.core.data.MapEntry.entry(StatusRelato.REJEITADO, 0L),
            org.assertj.core.data.MapEntry.entry(StatusRelato.CANCELADO, 0L));
    assertThat(estatisticas.porCategoria())
        .containsExactly(
            new TotalPorCategoria(1, "Iluminação", 6), new TotalPorCategoria(3, "Limpeza", 0));
  }

  @Test
  void baseVaziaConservaContratoDeZeros() {
    var estatisticas = composer.compor(0, List.of(), List.of());

    assertThat(estatisticas.total()).isZero();
    assertThat(estatisticas.porStatus())
        .hasSize(StatusRelato.values().length)
        .allSatisfy((status, total) -> assertThat(total).isZero());
    assertThat(estatisticas.porCategoria()).isEmpty();
  }
}

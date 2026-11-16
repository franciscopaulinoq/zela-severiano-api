package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO.TotalPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class EstatisticasConsultaIntegrationTest {

  @Autowired private RelatoService relatoService;

  @Autowired private CategoriaService categoriaService;

  @Test
  void composicaoConservaTotalStatusECategoriaSemRelatos() {
    var categoria =
        categoriaService.criar(
            new CategoriaRegistroDTO("Estatísticas " + UUID.randomUUID(), "estatistica"));

    var estatisticas = relatoService.obterEstatisticas();

    assertThat(estatisticas.total())
        .isEqualTo(estatisticas.porStatus().values().stream().mapToLong(Long::longValue).sum());
    assertThat(estatisticas.porStatus().keySet())
        .containsExactlyInAnyOrderElementsOf(Arrays.asList(StatusRelato.values()));
    assertThat(estatisticas.porCategoria().getLast())
        .isEqualTo(new TotalPorCategoria(categoria.getId(), categoria.getNome(), 0));
  }
}

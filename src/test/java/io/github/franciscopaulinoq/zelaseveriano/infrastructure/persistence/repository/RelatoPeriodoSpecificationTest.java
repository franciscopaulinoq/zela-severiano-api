package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoPeriodoSpecificationTest {
  @Autowired private RelatoService relatoService;
  @Autowired private JpaRelatoRepository relatoRepository;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void periodoTemInicioInclusivoEDiaSeguinteExclusivo() {
    TimeZone anterior = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("America/Fortaleza"));
      Cidadao cidadao = cidadaos.cadastrar("Cidadão dos Limites");
      Long noInicio = registrarEm(cidadao, "2026-05-02T03:00:00Z");
      registrarEm(cidadao, "2026-05-03T03:00:00Z");
      FiltroRelatos filtro =
          new FiltroRelatos(null, null, LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2));

      var resultado =
          relatoRepository.findAll(RelatoSpecifications.comFiltro(filtro), PageRequest.of(0, 10));

      assertThat(resultado)
          .singleElement()
          .extracting(relato -> relato.getId())
          .isEqualTo(noInicio);
    } finally {
      TimeZone.setDefault(anterior);
    }
  }

  @Test
  void construtorDeDoisArgumentosNaoRestringePeriodo() {
    FiltroRelatos filtro = new FiltroRelatos(StatusRelato.PENDENTE, 1);

    assertThat(filtro.dataInicial()).isNull();
    assertThat(filtro.dataFinal()).isNull();
  }

  private Long registrarEm(Cidadao cidadao, String criadoEm) {
    Long id =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Relato de teste", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    jdbcTemplate.update(
        "UPDATE relatos SET criado_em = ? WHERE id = ?", OffsetDateTime.parse(criadoEm), id);
    return id;
  }
}

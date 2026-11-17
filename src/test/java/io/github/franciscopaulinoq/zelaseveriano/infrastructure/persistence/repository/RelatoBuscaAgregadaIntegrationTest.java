package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import jakarta.persistence.EntityManagerFactory;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoBuscaAgregadaIntegrationTest {
  @Autowired private RelatoService relatoService;
  @Autowired private RelatoRepository relatoRepository;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private EntityManagerFactory entityManagerFactory;
  @Autowired private JdbcTemplate jdbcTemplate;

  private Statistics statistics;
  private boolean estatisticasHabilitadas;

  @BeforeEach
  void setUp() {
    statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    estatisticasHabilitadas = statistics.isStatisticsEnabled();
    statistics.setStatisticsEnabled(true);
  }

  @AfterEach
  void tearDown() {
    statistics.setStatisticsEnabled(estatisticasHabilitadas);
  }

  @Test
  void buscaAgregadaCarregaAssociacoesSemConsultasPorItem() {
    Cidadao cidadao = cidadaos.cadastrar("Autor da Consulta Agregada");
    Set<Long> ids = new LinkedHashSet<>();
    for (int indice = 0; indice < 4; indice++) {
      ids.add(registrar(cidadao));
    }
    statistics.clear();

    List<Relato> resultado = relatoRepository.findAllById(ids);

    assertThat(resultado).hasSize(4);
    assertThat(resultado)
        .allSatisfy(relato -> {
          assertThat(relato.getPerfil().getUsuario().getCpf()).isNotNull();
          assertThat(relato.getCategoria().getNome()).isNotBlank();
        });
    assertThat(statistics.getQueryExecutionCount()).isEqualTo(1);
  }

  @Test
  void buscaAgregadaOmiteSomenteOsIdsInexistentes() {
    Cidadao cidadao = cidadaos.cadastrar("Autor dos IDs Mistos");
    Long existente = registrar(cidadao);
    Set<Long> ids = new LinkedHashSet<>(Set.of(existente, 999999L));
    statistics.clear();

    List<Relato> resultado = relatoRepository.findAllById(ids);

    assertThat(resultado).singleElement().extracting(Relato::getId).isEqualTo(existente);
    assertThat(statistics.getQueryExecutionCount()).isEqualTo(1);
  }

  @Test
  void atualizacaoEmLoteUsaUmaUnicaInstrucaoETimestamp() {
    Cidadao cidadao = cidadaos.cadastrar("Autor da Atualização em Lote");
    Set<Long> ids = new LinkedHashSet<>();
    ids.add(registrar(cidadao));
    ids.add(registrar(cidadao));
    statistics.clear();

    int atualizados = relatoRepository.atualizarCategoriaEmLote(ids, 3, OffsetDateTime.now());

    assertThat(atualizados).isEqualTo(2);
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM relatos WHERE categoria_id = 3 AND id IN (?, ?)",
                Integer.class,
                ids.toArray()))
        .isEqualTo(2);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT atualizado_em) FROM relatos WHERE id IN (?, ?)",
                Integer.class,
                ids.toArray()))
        .isEqualTo(1);
  }

  @Test
  void atualizacaoEmLoteContaSomenteRegistrosEncontrados() {
    Cidadao cidadao = cidadaos.cadastrar("Autor da Contagem Parcial");
    Long existente = registrar(cidadao);
    Set<Long> ids = new LinkedHashSet<>(Set.of(existente, 999999L));
    statistics.clear();

    int atualizados = relatoRepository.atualizarCategoriaEmLote(ids, 3, OffsetDateTime.now());

    assertThat(atualizados).isEqualTo(1);
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
  }

  private Long registrar(Cidadao cidadao) {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(1, "Relato da consulta agregada", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }
}

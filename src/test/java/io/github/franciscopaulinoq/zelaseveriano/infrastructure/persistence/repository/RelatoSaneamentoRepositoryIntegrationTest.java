package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoSaneamentoRepositoryIntegrationTest {
  @Autowired private RelatoRepository repository;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private JdbcTemplate jdbcTemplate;

  private String base;
  private String prefixo;
  private Long relatoBase;

  @BeforeEach
  void setUp() {
    base = "https://cdn-" + UUID.randomUUID() + ".test/base";
    prefixo = base + "//";
    Cidadao autor = cidadaos.cadastrar("Autor da consulta de saneamento");
    relatoBase =
        relatoService
            .registrar(
                autor.usuarioId(),
                new RelatoRegistroDTO(1, "Consulta de URLs", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    List<Object[]> candidatos =
        IntStream.range(0, 75)
            .mapToObj(i -> new Object[] {prefixo + i + ".jpg", relatoBase})
            .toList();
    jdbcTemplate.batchUpdate(
        """
        INSERT INTO relatos (perfil_id, categoria_id, status, descricao, url_foto, latitude, longitude)
        SELECT perfil_id, categoria_id, status, descricao, ?, latitude, longitude
        FROM relatos WHERE id = ?
        """,
        candidatos);
    jdbcTemplate.update(
        "UPDATE relatos SET url_foto = ? WHERE id = ?",
        "https://outro.test/base//externa.jpg",
        relatoBase);
  }

  @Test
  void consultaFiltraCandidatosEOrdenaPorId() {
    Page<Relato> pagina = repository.findByUrlFotoStartingWith(prefixo, PageRequest.of(0, 50));

    assertThat(pagina.getTotalElements()).isEqualTo(75);
    assertThat(pagina.getContent()).hasSize(50);
    assertThat(pagina.getContent())
        .allSatisfy(relato -> assertThat(relato.getUrlFoto()).startsWith(prefixo));
    assertThat(pagina.getContent())
        .isSortedAccordingTo(java.util.Comparator.comparing(Relato::getId));
  }

  @Test
  void segundaPaginaContemSomenteOsCandidatosRestantes() {
    Page<Relato> pagina = repository.findByUrlFotoStartingWith(prefixo, PageRequest.of(1, 50));

    assertThat(pagina.getContent()).hasSize(25);
    assertThat(pagina.getTotalPages()).isEqualTo(2);
    assertThat(pagina.getContent())
        .allSatisfy(relato -> assertThat(relato.getUrlFoto()).startsWith(prefixo));
  }

  @Test
  void urlsNormalizadasEExternasNaoEntramNaConsulta() {
    jdbcTemplate.update(
        "UPDATE relatos SET url_foto = ? WHERE id IN (SELECT id FROM relatos LIMIT 1)",
        base + "/normalizada.jpg");

    Page<Relato> pagina = repository.findByUrlFotoStartingWith(prefixo, PageRequest.of(0, 50));

    assertThat(pagina.getContent())
        .noneMatch(relato -> relato.getUrlFoto().equals(base + "/normalizada.jpg"));
    assertThat(pagina.getContent())
        .noneMatch(relato -> "https://outro.test/base//externa.jpg".equals(relato.getUrlFoto()));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class SaneamentoUrlFotoIntegrationTest {
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private JdbcTemplate jdbcTemplate;

  private Long duplicada;
  private Long normalizada;
  private Long externa;

  @BeforeEach
  void setUp() {
    Cidadao autor = cidadaos.cadastrar("Autor do saneamento");
    duplicada = registrar(autor);
    normalizada = registrar(autor);
    externa = registrar(autor);
    atualizarUrl(duplicada, "https://cdn.test/base//foto.jpg");
    atualizarUrl(normalizada, "https://cdn.test/base/foto.jpg");
    atualizarUrl(externa, "https://outro.test/base//foto.jpg");
  }

  @Test
  void saneamentoCorrigeSomenteCandidatasEahIdempotente() {
    int atualizados = relatoService.sanearUrlsFotos("https://cdn.test/base/");

    assertThat(atualizados).isEqualTo(1);
    assertThat(url(duplicada)).isEqualTo("https://cdn.test/base/foto.jpg");
    assertThat(url(normalizada)).isEqualTo("https://cdn.test/base/foto.jpg");
    assertThat(url(externa)).isEqualTo("https://outro.test/base//foto.jpg");
    assertThat(relatoService.sanearUrlsFotos("https://cdn.test/base")).isZero();
  }

  @Test
  void processaMaisDeDoisLotesSemIgnorarRegistros() {
    var novos =
        java.util.stream.IntStream.range(0, 120)
            .mapToObj(i -> new Object[] {"https://cdn.test/base//lote-" + i + ".jpg", duplicada})
            .toList();
    jdbcTemplate.batchUpdate(
        """
        INSERT INTO relatos (perfil_id, categoria_id, status, descricao, url_foto, latitude, longitude)
        SELECT perfil_id, categoria_id, status, descricao, ?, latitude, longitude
        FROM relatos WHERE id = ?
        """,
        novos);

    assertThat(relatoService.sanearUrlsFotos("https://cdn.test/base/")).isEqualTo(121);
    Integer pendentes =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM relatos WHERE url_foto LIKE 'https://cdn.test/base//%'",
            Integer.class);
    assertThat(pendentes).isZero();
  }

  private Long registrar(Cidadao autor) {
    return relatoService
        .registrar(
            autor.usuarioId(),
            new RelatoRegistroDTO(1, "Foto para saneamento", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private void atualizarUrl(Long relatoId, String url) {
    jdbcTemplate.update("UPDATE relatos SET url_foto = ? WHERE id = ?", url, relatoId);
  }

  private String url(Long relatoId) {
    return jdbcTemplate.queryForObject(
        "SELECT url_foto FROM relatos WHERE id = ?", String.class, relatoId);
  }
}

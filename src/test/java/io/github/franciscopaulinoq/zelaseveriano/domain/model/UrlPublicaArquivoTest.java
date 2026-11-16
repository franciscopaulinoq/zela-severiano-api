package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlPublicaArquivoTest {
  @ParameterizedTest
  @ValueSource(
      strings = {"https://cdn.test/base", "https://cdn.test/base/", "https://cdn.test/base///"})
  void compoeUrlComUmaBarraIndependenteDaBase(String base) {
    assertThat(UrlPublicaArquivo.compor(base, "pasta/foto.jpg"))
        .isEqualTo("https://cdn.test/base/pasta/foto.jpg");
  }

  @Test
  void normalizaSomenteSeparadorEntreBaseEChave() {
    String url = "https://cdn.test/base//pasta/foto.jpg";

    assertThat(UrlPublicaArquivo.normalizar("https://cdn.test/base/", url))
        .isEqualTo("https://cdn.test/base/pasta/foto.jpg");
  }

  @Test
  void naoAlteraUrlNormalizadaOuDeOutroDominio() {
    assertThat(
            UrlPublicaArquivo.normalizar("https://cdn.test/base", "https://cdn.test/base/foto.jpg"))
        .isEqualTo("https://cdn.test/base/foto.jpg");
    assertThat(
            UrlPublicaArquivo.normalizar("https://cdn.test/base", "https://outro.test//foto.jpg"))
        .isEqualTo("https://outro.test//foto.jpg");
  }

  @Test
  void removeEspacosAcidentaisDaBase() {
    assertThat(UrlPublicaArquivo.compor("  https://cdn.test/base/  ", "foto.jpg"))
        .isEqualTo("https://cdn.test/base/foto.jpg");
  }

  @Test
  void prefixoDeCandidatosNaoConfundeProtocolo() {
    String prefixo = UrlPublicaArquivo.prefixoCandidatos("https://cdn.test/base/");

    assertThat(prefixo).isEqualTo("https://cdn.test/base//");
    assertThat(prefixo).doesNotStartWith("https:///");
  }
}

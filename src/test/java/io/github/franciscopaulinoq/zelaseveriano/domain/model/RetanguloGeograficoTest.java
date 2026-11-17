package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class RetanguloGeograficoTest {
  private static final RetanguloGeografico RETANGULO = new RetanguloGeografico(-10, -5, -20, -15);

  @ParameterizedTest
  @CsvSource({"-7.5, -17.5", "-10, -17.5", "-7.5, -20", "-10, -20", "-5.000001, -15.000001"})
  void contemPontosInternosEFronteirasInferiores(double latitude, double longitude) {
    assertThat(RETANGULO.contem(latitude, longitude)).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "-10.000001, -17.5",
    "-4.999999, -17.5",
    "-7.5, -20.000001",
    "-7.5, -14.999999",
    "-11, -21",
    "-4, -14"
  })
  void naoContemPontosForaDoRetangulo(double latitude, double longitude) {
    assertThat(RETANGULO.contem(latitude, longitude)).isFalse();
  }

  @ParameterizedTest
  @CsvSource(
      value = {"null, -7.5", "-7.5, null", "null, null"},
      nullValues = "null")
  void coordenadaAusenteNaoEhContida(Double latitude, Double longitude) {
    assertThat(RETANGULO.contem(latitude, longitude)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("coordenadasNaoFinitas")
  void coordenadasNaoFinitasNaoSaoContidas(double latitude, double longitude) {
    assertThat(RETANGULO.contem(latitude, longitude)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("limitesNaoFinitos")
  void construtorRecusaLimitesNaoFinitos(
      double latitudeMinima,
      double latitudeMaxima,
      double longitudeMinima,
      double longitudeMaxima) {
    assertThatThrownBy(() ->
            new RetanguloGeografico(
                latitudeMinima, latitudeMaxima, longitudeMinima, longitudeMaxima))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("finitos");
  }

  @Test
  void construtorRecusaLatitudesInvertidas() {
    assertThatThrownBy(() -> new RetanguloGeografico(-5, -10, -20, -15))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invertidos");
  }

  @Test
  void construtorRecusaLongitudesInvertidas() {
    assertThatThrownBy(() -> new RetanguloGeografico(-10, -5, -15, -20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invertidos");
  }

  @ParameterizedTest
  @CsvSource({"-10, -5, -20, -15", "0, 10, 0, 10", "-90, 90, -180, 180"})
  void construtorPreservaOsLimitesRecebidos(
      double latitudeMinima,
      double latitudeMaxima,
      double longitudeMinima,
      double longitudeMaxima) {
    RetanguloGeografico retangulo =
        new RetanguloGeografico(latitudeMinima, latitudeMaxima, longitudeMinima, longitudeMaxima);

    assertThat(retangulo.latitudeMinima()).isEqualTo(latitudeMinima);
    assertThat(retangulo.latitudeMaxima()).isEqualTo(latitudeMaxima);
    assertThat(retangulo.longitudeMinima()).isEqualTo(longitudeMinima);
    assertThat(retangulo.longitudeMaxima()).isEqualTo(longitudeMaxima);
  }

  @ParameterizedTest
  @CsvSource({"0, 10, 0, 10, 5, 5", "-90, 90, -180, 180, 0, 0", "-90, 90, -180, 180, -90, -180"})
  void regraFuncionaParaRetangulosDeOutrasRegioes(
      double latitudeMinima,
      double latitudeMaxima,
      double longitudeMinima,
      double longitudeMaxima,
      double latitude,
      double longitude) {
    RetanguloGeografico retangulo =
        new RetanguloGeografico(latitudeMinima, latitudeMaxima, longitudeMinima, longitudeMaxima);

    assertThat(retangulo.contem(latitude, longitude)).isTrue();
  }

  private static Stream<Arguments> coordenadasNaoFinitas() {
    return Stream.of(
        Arguments.of(Double.NaN, -17.5),
        Arguments.of(-7.5, Double.NaN),
        Arguments.of(Double.POSITIVE_INFINITY, -17.5),
        Arguments.of(Double.NEGATIVE_INFINITY, -17.5),
        Arguments.of(-7.5, Double.POSITIVE_INFINITY),
        Arguments.of(-7.5, Double.NEGATIVE_INFINITY));
  }

  private static Stream<Arguments> limitesNaoFinitos() {
    return Stream.of(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        .flatMap(valor ->
            Stream.of(
                Arguments.of(valor, -5, -20, -15),
                Arguments.of(-10, valor, -20, -15),
                Arguments.of(-10, -5, valor, -15),
                Arguments.of(-10, -5, -20, valor)));
  }
}

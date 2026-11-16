package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LimitesDoMunicipioTest {

  @ParameterizedTest
  @CsvSource({
    "-6.08, -38.37",
    "-6.15, -38.45",
    "-6.20, -38.52",
    "-6.20, -38.38",
    "-6.08, -38.52",
    "-5.960001, -38.240001"
  })
  void aceitaPontosRepresentativosDoMunicipio(double latitude, double longitude) {
    assertThat(LimitesDoMunicipio.contem(latitude, longitude)).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "-5.79, -35.21",
    "-6.200001, -38.38",
    "-5.959999, -38.38",
    "-6.08, -38.520001",
    "-6.08, -38.239999",
    "0, 0"
  })
  void recusaPontosForaDoMunicipio(double latitude, double longitude) {
    assertThat(LimitesDoMunicipio.contem(latitude, longitude)).isFalse();
  }

  @ParameterizedTest
  @CsvSource(
      value = {"null, -38.38", "-6.08, null", "null, null"},
      nullValues = "null")
  void recusaCoordenadasAusentes(Double latitude, Double longitude) {
    assertThat(LimitesDoMunicipio.contem(latitude, longitude)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void recusaCoordenadasNaoFinitas(double valor) {
    assertThat(LimitesDoMunicipio.contem(valor, -38.38)).isFalse();
    assertThat(LimitesDoMunicipio.contem(-6.08, valor)).isFalse();
  }
}

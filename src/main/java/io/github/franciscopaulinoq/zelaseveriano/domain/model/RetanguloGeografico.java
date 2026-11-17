package io.github.franciscopaulinoq.zelaseveriano.domain.model;

public record RetanguloGeografico(
    double latitudeMinima, double latitudeMaxima, double longitudeMinima, double longitudeMaxima) {

  public RetanguloGeografico {
    if (!Double.isFinite(latitudeMinima)
        || !Double.isFinite(latitudeMaxima)
        || !Double.isFinite(longitudeMinima)
        || !Double.isFinite(longitudeMaxima)) {
      throw new IllegalArgumentException("Os limites geográficos devem ser finitos.");
    }
    if (latitudeMinima > latitudeMaxima || longitudeMinima > longitudeMaxima) {
      throw new IllegalArgumentException("Os limites geográficos estão invertidos.");
    }
  }

  public boolean contem(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
      return false;
    }
    return latitude >= latitudeMinima
        && latitude < latitudeMaxima
        && longitude >= longitudeMinima
        && longitude < longitudeMaxima;
  }
}

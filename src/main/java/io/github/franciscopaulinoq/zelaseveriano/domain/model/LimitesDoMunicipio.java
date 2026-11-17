package io.github.franciscopaulinoq.zelaseveriano.domain.model;

/**
 * Retângulo aproximado que contém o município de Doutor Severiano/RN (sede em 6,08° S, 38,37° O),
 * com folga para cobrir a zona rural. Serve para descartar coordenadas claramente erradas, não para
 * validar o limite exato.
 */
public final class LimitesDoMunicipio {

  private static final RetanguloGeografico RETANGULO =
      new RetanguloGeografico(-6.20, -5.96, -38.52, -38.24);

  private LimitesDoMunicipio() {}

  public static boolean contem(Double latitude, Double longitude) {
    return RETANGULO.contem(latitude, longitude);
  }
}

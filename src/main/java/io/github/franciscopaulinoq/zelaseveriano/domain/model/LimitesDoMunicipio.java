package io.github.franciscopaulinoq.zelaseveriano.domain.model;

/**
 * Retângulo aproximado que contém o município de Doutor Severiano/RN (sede em 6,08° S, 38,37° O), com folga para
 * cobrir a zona rural. Serve para descartar coordenadas claramente erradas, não para validar o limite exato.
 */
public final class LimitesDoMunicipio {

    public static final double LATITUDE_MINIMA = -6.20;
    public static final double LATITUDE_MAXIMA = -5.96;
    public static final double LONGITUDE_MINIMA = -38.52;
    public static final double LONGITUDE_MAXIMA = -38.24;

    private LimitesDoMunicipio() {}

    public static boolean contem(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= LATITUDE_MINIMA
                && latitude <= LATITUDE_MAXIMA
                && longitude >= LONGITUDE_MINIMA
                && longitude <= LONGITUDE_MAXIMA;
    }
}

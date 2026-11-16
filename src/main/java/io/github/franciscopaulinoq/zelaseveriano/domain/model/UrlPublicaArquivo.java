package io.github.franciscopaulinoq.zelaseveriano.domain.model;

public final class UrlPublicaArquivo {
  private UrlPublicaArquivo() {}

  public static String compor(String basePublica, String chave) {
    return baseNormalizada(basePublica) + "/" + chave;
  }

  public static String prefixoCandidatos(String basePublica) {
    return baseNormalizada(basePublica) + "//";
  }

  public static String normalizar(String basePublica, String url) {
    String prefixo = prefixoCandidatos(basePublica);
    if (!url.startsWith(prefixo)) {
      return url;
    }
    return compor(basePublica, url.substring(prefixo.length()));
  }

  private static String baseNormalizada(String basePublica) {
    return basePublica.strip().replaceAll("/+$", "");
  }
}

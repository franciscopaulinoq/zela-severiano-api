package io.github.franciscopaulinoq.zelaseveriano.domain.storage;

public record ArquivoArmazenado(byte[] conteudo, String tipoConteudo) {
  public ArquivoArmazenado {
    if (tipoConteudo == null || tipoConteudo.isBlank()) {
      tipoConteudo = "application/octet-stream";
    }
  }
}

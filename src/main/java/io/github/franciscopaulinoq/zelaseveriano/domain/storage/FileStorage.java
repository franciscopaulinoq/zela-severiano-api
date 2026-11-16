package io.github.franciscopaulinoq.zelaseveriano.domain.storage;

public interface FileStorage {
  String upload(byte[] bytes, String fileName, String contentType);

  void excluir(String urlFoto);
}

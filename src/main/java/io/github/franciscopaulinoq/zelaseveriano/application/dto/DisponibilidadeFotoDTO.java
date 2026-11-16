package io.github.franciscopaulinoq.zelaseveriano.application.dto;

public record DisponibilidadeFotoDTO(Long relatoId, SituacaoFoto situacao) {
  public enum SituacaoFoto {
    DISPONIVEL,
    INDISPONIVEL,
    SEM_FOTO
  }
}

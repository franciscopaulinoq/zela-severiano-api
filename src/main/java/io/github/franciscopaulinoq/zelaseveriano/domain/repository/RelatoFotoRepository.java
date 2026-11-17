package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RelatoFoto;

public interface RelatoFotoRepository {
  RelatoFoto save(RelatoFoto foto);

  long countByRelatoId(Long relatoId);
}

package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Vistoria;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VistoriaRepository {
  Vistoria save(Vistoria vistoria);

  Optional<Vistoria> findById(Long vistoriaId);

  boolean existeConflito(UUID gestorId, OffsetDateTime inicio, OffsetDateTime fim);

  List<Vistoria> findAllByRelatoIdOrderByInicio(Long relatoId);
}

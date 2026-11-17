package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.DetalheGestaoRelato;
import java.util.Optional;

public interface RelatoDetalheRepository {
  Optional<DetalheGestaoRelato> buscarDetalheCompleto(Long relatoId);
}

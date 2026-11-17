package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.AuditoriaGestao;
import java.util.List;
import java.util.UUID;

public interface AuditoriaGestaoRepository {
  AuditoriaGestao save(AuditoriaGestao auditoria);

  List<AuditoriaGestao> consultar(UUID gestorId, String metodo);
}

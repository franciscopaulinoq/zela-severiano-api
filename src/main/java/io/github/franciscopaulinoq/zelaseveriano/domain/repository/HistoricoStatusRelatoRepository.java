package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.HistoricoStatusRelato;
import java.util.List;

public interface HistoricoStatusRelatoRepository {
    HistoricoStatusRelato save(HistoricoStatusRelato historico);

    List<HistoricoStatusRelato> findAllByRelatoId(Long relatoId);
}

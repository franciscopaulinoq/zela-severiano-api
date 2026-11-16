package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.HistoricoStatusRelatoEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaHistoricoStatusRelatoRepository
    extends JpaRepository<HistoricoStatusRelatoEntity, Long> {
  List<HistoricoStatusRelatoEntity> findAllByRelatoIdOrderByAlteradoEmAscIdAsc(Long relatoId);

  List<HistoricoStatusRelatoEntity> findAllByIdGreaterThanOrderByIdAsc(long id, Pageable pageable);

  @Query("SELECT COALESCE(MAX(h.id), 0) FROM HistoricoStatusRelatoEntity h")
  long maxId();
}

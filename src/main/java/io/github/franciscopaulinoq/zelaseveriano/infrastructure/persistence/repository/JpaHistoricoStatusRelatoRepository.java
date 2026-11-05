package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.HistoricoStatusRelatoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHistoricoStatusRelatoRepository extends JpaRepository<HistoricoStatusRelatoEntity, Long> {
    List<HistoricoStatusRelatoEntity> findAllByRelatoIdOrderByAlteradoEmAscIdAsc(Long relatoId);
}

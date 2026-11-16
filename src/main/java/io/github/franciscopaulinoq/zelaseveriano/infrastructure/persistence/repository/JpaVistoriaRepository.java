package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.VistoriaEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaVistoriaRepository extends JpaRepository<VistoriaEntity, Long> {
  boolean existsByGestorIdAndStatusAndInicioLessThanAndFimGreaterThan(
      UUID gestorId, StatusVistoria status, OffsetDateTime fim, OffsetDateTime inicio);

  List<VistoriaEntity> findAllByRelatoIdOrderByInicio(Long relatoId);
}

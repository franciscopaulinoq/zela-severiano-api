package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRelatoFotoRepository extends JpaRepository<RelatoFotoEntity, Long> {
  long countByRelatoId(Long relatoId);
}

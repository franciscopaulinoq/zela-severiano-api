package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRelatoRepository extends JpaRepository<RelatoEntity, Long> {
    Optional<RelatoEntity> findByPerfilIdAndId(UUID perfilId, Long relatoId);
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.PerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPerfilRepository extends JpaRepository<PerfilEntity, UUID> {
    public Optional<PerfilEntity> findByUsuarioId(UUID usuarioId);
}

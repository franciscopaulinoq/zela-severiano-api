package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.PerfilEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPerfilRepository extends JpaRepository<PerfilEntity, UUID> {
    Optional<PerfilEntity> findByUsuarioId(UUID usuarioId);
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.PerfilEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaPerfilRepository extends JpaRepository<PerfilEntity, UUID> {
  Optional<PerfilEntity> findByUsuarioId(UUID usuarioId);

  @Query(value = "SELECT id FROM perfis WHERE id = :perfilId FOR UPDATE", nativeQuery = true)
  UUID bloquear(UUID perfilId);
}

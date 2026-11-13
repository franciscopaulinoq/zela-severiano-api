package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRelatoRepository extends JpaRepository<RelatoEntity, Long> {
    Optional<RelatoEntity> findByPerfilIdAndId(UUID perfilId, Long relatoId);

    @EntityGraph(attributePaths = "categoria")
    List<RelatoEntity> findAllByPerfilIdOrderByCriadoEmDesc(UUID perfilId);

    @Override
    @EntityGraph(attributePaths = {"perfil", "categoria"})
    List<RelatoEntity> findAll();
}

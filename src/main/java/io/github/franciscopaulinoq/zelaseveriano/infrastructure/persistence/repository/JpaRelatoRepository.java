package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaRelatoRepository extends JpaRepository<RelatoEntity, Long>, JpaSpecificationExecutor<RelatoEntity> {
    Optional<RelatoEntity> findByPerfilIdAndId(UUID perfilId, Long relatoId);

    long countByPerfilIdAndStatus(UUID perfilId, StatusRelato status);

    boolean existsByCategoriaIdAndStatus(Integer categoriaId, StatusRelato status);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<RelatoEntity> findAll(Specification<RelatoEntity> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"perfil", "categoria"})
    List<RelatoEntity> findAll(Specification<RelatoEntity> spec, Sort sort);
}

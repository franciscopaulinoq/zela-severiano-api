package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
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
import org.springframework.data.jpa.repository.Query;

public interface JpaRelatoRepository
    extends JpaRepository<RelatoEntity, Long>, JpaSpecificationExecutor<RelatoEntity> {
  Optional<RelatoEntity> findByPerfilIdAndId(UUID perfilId, Long relatoId);

  long countByPerfilIdAndStatus(UUID perfilId, StatusRelato status);

  boolean existsByCategoriaId(Integer categoriaId);

  @Query(
      """
      SELECT new io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus(
          r.status, COUNT(r))
      FROM RelatoEntity r
      GROUP BY r.status
      """)
  List<TotalRelatosPorStatus> contarPorStatus();

  @Query(
      """
      SELECT new io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria(
          c.id, c.nome, COUNT(r.id))
      FROM CategoriaEntity c
      LEFT JOIN RelatoEntity r ON r.categoria = c
      GROUP BY c.id, c.nome
      ORDER BY c.id
      """)
  List<TotalRelatosPorCategoria> contarPorCategoria();

  @Override
  @EntityGraph(attributePaths = "categoria")
  Page<RelatoEntity> findAll(Specification<RelatoEntity> spec, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"perfil", "categoria"})
  List<RelatoEntity> findAll(Specification<RelatoEntity> spec, Sort sort);
}

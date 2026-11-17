package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JpaRelatoRepository
    extends JpaRepository<RelatoEntity, Long>, JpaSpecificationExecutor<RelatoEntity> {
  boolean existsByPerfilIdAndId(UUID perfilId, Long relatoId);

  Optional<RelatoEntity> findByPerfilIdAndId(UUID perfilId, Long relatoId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM RelatoEntity r WHERE r.id = :relatoId")
  Optional<RelatoEntity> findByIdParaAtualizacao(Long relatoId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM RelatoEntity r WHERE r.perfil.id = :perfilId AND r.id = :relatoId")
  Optional<RelatoEntity> findByPerfilIdAndIdParaAtualizacao(UUID perfilId, Long relatoId);

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
  @EntityGraph(attributePaths = {"perfil", "categoria"})
  Page<RelatoEntity> findAll(Specification<RelatoEntity> spec, Pageable pageable);
}

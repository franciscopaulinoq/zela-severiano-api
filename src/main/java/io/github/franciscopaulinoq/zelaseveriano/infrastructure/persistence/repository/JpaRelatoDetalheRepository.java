package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaRelatoDetalheRepository extends JpaRepository<RelatoEntity, Long> {
  @Query(
      value =
          """
          SELECT r.*
          FROM relatos r
          LEFT JOIN relato_fotos_complementares f ON f.relato_id = r.id
          LEFT JOIN comentarios_relato c ON c.relato_id = r.id
          WHERE r.id = :relatoId
          """,
      nativeQuery = true)
  List<RelatoEntity> buscarDetalheCompleto(Long relatoId);
}

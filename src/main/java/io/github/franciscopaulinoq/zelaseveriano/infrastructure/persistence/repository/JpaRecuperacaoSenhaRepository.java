package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RecuperacaoSenhaEntity;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenhaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select r from RecuperacaoSenhaEntity r "
          + "where r.usuarioId = :usuarioId and r.codigo = :codigo and r.invalidadoEm is null")
  Optional<RecuperacaoSenhaEntity> findAtivaPorUsuarioIdECodigoParaAtualizacao(
      @Param("usuarioId") UUID usuarioId, @Param("codigo") String codigo);

  @Modifying
  @Query(
      "update RecuperacaoSenhaEntity r set r.invalidadoEm = :momento "
          + "where r.usuarioId = :usuarioId and r.invalidadoEm is null")
  void invalidarAnteriores(
      @Param("usuarioId") UUID usuarioId, @Param("momento") OffsetDateTime momento);
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.SolicitacaoAlteracaoEmailEntity;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JpaSolicitacaoAlteracaoEmailRepository
    extends JpaRepository<SolicitacaoAlteracaoEmailEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT s FROM SolicitacaoAlteracaoEmailEntity s "
          + "WHERE s.perfilId = :perfilId AND s.codigoHash = :codigoHash "
          + "AND s.consumidoEm IS NULL")
  Optional<SolicitacaoAlteracaoEmailEntity> findValidacao(UUID perfilId, String codigoHash);

  @Modifying
  @Query(
      "UPDATE SolicitacaoAlteracaoEmailEntity s SET s.consumidoEm = :consumidoEm "
          + "WHERE s.perfilId = :perfilId AND s.consumidoEm IS NULL")
  void revogarAnteriores(UUID perfilId, OffsetDateTime consumidoEm);
}

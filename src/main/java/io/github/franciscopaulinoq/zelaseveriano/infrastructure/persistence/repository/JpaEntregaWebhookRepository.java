package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EntregaWebhookEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaEntregaWebhookRepository extends JpaRepository<EntregaWebhookEntity, UUID> {
  @Query(
      "SELECT e FROM EntregaWebhookEntity e "
          + "WHERE e.entregueEm IS NULL AND e.proximaTentativaEm <= :agora "
          + "ORDER BY e.proximaTentativaEm, e.id")
  List<EntregaWebhookEntity> findPendentes(OffsetDateTime agora, Pageable pageable);
}

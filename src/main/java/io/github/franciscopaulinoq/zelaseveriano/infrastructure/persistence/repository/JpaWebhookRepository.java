package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.WebhookEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JpaWebhookRepository extends JpaRepository<WebhookEntity, UUID> {
  List<WebhookEntity> findAllByAtivoTrue();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT w FROM WebhookEntity w WHERE w.id = :id")
  Optional<WebhookEntity> findByIdParaAtualizacao(UUID id);
}

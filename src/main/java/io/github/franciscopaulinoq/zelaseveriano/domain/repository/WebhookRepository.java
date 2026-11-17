package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Webhook;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookRepository {
  Webhook save(Webhook webhook);

  Optional<Webhook> findByIdParaAtualizacao(UUID id);

  List<Webhook> findAll();

  List<Webhook> findAllByAtivoTrue();
}

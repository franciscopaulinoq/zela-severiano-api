package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import java.time.OffsetDateTime;
import java.util.List;

public interface EntregaWebhookRepository {
  EntregaWebhook save(EntregaWebhook entrega);

  List<EntregaWebhook> saveAll(List<EntregaWebhook> entregas);

  List<EntregaWebhook> findPendentes(OffsetDateTime agora, int limite);
}

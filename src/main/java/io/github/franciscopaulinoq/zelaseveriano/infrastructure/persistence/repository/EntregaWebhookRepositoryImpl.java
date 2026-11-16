package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EntregaWebhookRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.EntregaWebhookMapper;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EntregaWebhookRepositoryImpl implements EntregaWebhookRepository {
  private final JpaEntregaWebhookRepository jpaRepository;
  private final EntregaWebhookMapper mapper;

  @Override
  public EntregaWebhook save(EntregaWebhook entrega) {
    return mapper.map(jpaRepository.save(mapper.map(entrega)));
  }

  @Override
  public List<EntregaWebhook> saveAll(List<EntregaWebhook> entregas) {
    return jpaRepository.saveAll(entregas.stream().map(mapper::map).toList()).stream()
        .map(mapper::map)
        .toList();
  }

  @Override
  public List<EntregaWebhook> findPendentes(OffsetDateTime agora, int limite) {
    return jpaRepository.findPendentes(agora, PageRequest.of(0, limite)).stream()
        .map(mapper::map)
        .toList();
  }
}

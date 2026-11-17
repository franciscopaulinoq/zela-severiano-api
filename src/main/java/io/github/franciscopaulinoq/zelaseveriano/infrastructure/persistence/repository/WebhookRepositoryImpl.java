package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Webhook;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.WebhookRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.WebhookMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WebhookRepositoryImpl implements WebhookRepository {
  private final JpaWebhookRepository jpaRepository;
  private final WebhookMapper mapper;

  @Override
  public Webhook save(Webhook webhook) {
    return mapper.map(jpaRepository.save(mapper.map(webhook)));
  }

  @Override
  public Optional<Webhook> findByIdParaAtualizacao(UUID id) {
    return jpaRepository.findByIdParaAtualizacao(id).map(mapper::map);
  }

  @Override
  public List<Webhook> findAll() {
    return jpaRepository.findAll().stream().map(mapper::map).toList();
  }

  @Override
  public List<Webhook> findAllByAtivoTrue() {
    return jpaRepository.findAllByAtivoTrue().stream().map(mapper::map).toList();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Webhook;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.WebhookEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookMapper {
  WebhookEntity map(Webhook webhook);

  Webhook map(WebhookEntity entity);
}

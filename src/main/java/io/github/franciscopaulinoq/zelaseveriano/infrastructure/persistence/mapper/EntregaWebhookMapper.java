package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.EntregaWebhook;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EntregaWebhookEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntregaWebhookMapper {
  EntregaWebhookEntity map(EntregaWebhook entrega);

  EntregaWebhook map(EntregaWebhookEntity entity);
}

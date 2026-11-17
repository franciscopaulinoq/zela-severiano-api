package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.AuditoriaGestao;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AuditoriaGestaoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaGestaoMapper {
  AuditoriaGestaoEntity map(AuditoriaGestao auditoria);

  AuditoriaGestao map(AuditoriaGestaoEntity auditoria);
}

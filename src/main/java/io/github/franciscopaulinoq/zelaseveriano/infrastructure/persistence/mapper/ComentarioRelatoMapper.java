package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.ComentarioRelato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.ComentarioRelatoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComentarioRelatoMapper {
  @Mapping(target = "relato", ignore = true)
  ComentarioRelatoEntity map(ComentarioRelato comentario);

  @Mapping(target = "relatoId", source = "relato.id")
  ComentarioRelato map(ComentarioRelatoEntity comentario);
}

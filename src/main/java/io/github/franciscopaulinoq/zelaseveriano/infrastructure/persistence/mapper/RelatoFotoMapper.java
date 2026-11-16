package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RelatoFoto;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoFotoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RelatoFotoMapper {
  @Mapping(target = "relato", ignore = true)
  RelatoFotoEntity map(RelatoFoto foto);

  @Mapping(target = "relatoId", source = "relato.id")
  RelatoFoto map(RelatoFotoEntity foto);
}

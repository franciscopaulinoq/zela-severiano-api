package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Aviso;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AvisoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvisoMapper {
  AvisoEntity map(Aviso aviso);

  Aviso map(AvisoEntity avisoEntity);
}

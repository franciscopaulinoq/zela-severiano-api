package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Vistoria;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.VistoriaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VistoriaMapper {
  VistoriaEntity map(Vistoria vistoria);

  Vistoria map(VistoriaEntity vistoriaEntity);
}

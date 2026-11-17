package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.AvisoLeitura;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AvisoLeituraEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {AvisoMapper.class, PerfilMapper.class})
public interface AvisoLeituraMapper {
  AvisoLeituraEntity map(AvisoLeitura leitura);

  @Mapping(target = "marcarComoLida", ignore = true)
  AvisoLeitura map(AvisoLeituraEntity leituraEntity);
}

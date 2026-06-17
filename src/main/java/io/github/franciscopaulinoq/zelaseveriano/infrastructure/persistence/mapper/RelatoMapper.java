package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { CategoriaMapper.class, PerfilMapper.class })
public interface RelatoMapper {
    RelatoEntity map(Relato relato);

    Relato map(RelatoEntity relatoEntity);
}

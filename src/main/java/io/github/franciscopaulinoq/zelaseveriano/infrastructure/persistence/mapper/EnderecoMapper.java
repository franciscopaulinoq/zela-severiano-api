package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EnderecoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { PerfilMapper.class })
public interface EnderecoMapper {
    EnderecoEntity map(Endereco endereco);

    Endereco map(EnderecoEntity enderecoEntity);
}

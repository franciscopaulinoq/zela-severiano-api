package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.PerfilEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { UsuarioMapper.class })
public interface PerfilMapper {
    PerfilEntity map(Perfil perfil);

    Perfil map(PerfilEntity perfilEntity);
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RegistrarCidadaoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RegistrarCidadaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrarCidadaoMapper {

    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "perfilId", ignore = true)
    @Mapping(target = "enderecoId", ignore = true)
    @Mapping(target = "senhaHash", ignore = true)
    RegistrarCidadaoDTO map(RegistrarCidadaoRequest request);

    @Mapping(source = "usuarioId", target = "usuarioId")
    @Mapping(source = "perfilId", target = "perfilId")
    @Mapping(source = "enderecoId", target = "enderecoId")
    @Mapping(target = "mensagem", expression = "java(\"Cidadão registrado com sucesso\")")
    RegistrarCidadaoResponse map(RegistrarCidadaoDTO dto);
}

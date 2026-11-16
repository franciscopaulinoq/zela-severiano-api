package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RegistrarCidadaoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RegistrarCidadaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.CidadaoRegistradoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrarCidadaoMapper {

  RegistrarCidadaoDTO map(RegistrarCidadaoRequest request);

  @Mapping(target = "mensagem", expression = "java(\"Cidadão registrado com sucesso\")")
  RegistrarCidadaoResponse map(CidadaoRegistradoDTO dto);
}

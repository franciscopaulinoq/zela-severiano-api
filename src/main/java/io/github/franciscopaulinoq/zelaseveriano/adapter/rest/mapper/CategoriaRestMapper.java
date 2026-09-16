package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.CategoriaResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaRestMapper {
    CategoriaResponse map(CategoriaDTO dto);
    List<CategoriaResponse> map(List<CategoriaDTO> dtos);
}

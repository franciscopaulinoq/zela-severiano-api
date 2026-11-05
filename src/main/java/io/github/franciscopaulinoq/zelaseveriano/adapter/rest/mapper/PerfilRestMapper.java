package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.AtualizarPerfilRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.PerfilMeResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarPerfilDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PerfilRestMapper {
    PerfilMeResponse map(PerfilMeDTO dto);

    AtualizarPerfilDTO map(AtualizarPerfilRequest request);
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RelatoRegistroRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoCriadoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RelatoRestMapper {
    RelatoRegistroDTO map(RelatoRegistroRequest request);
    RelatoCriadoResponse map(Relato relato);
}
package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.HistoricoStatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.HistoricoStatusRelatoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoricoStatusRelatoMapper {
    HistoricoStatusRelatoEntity map(HistoricoStatusRelato historico);

    HistoricoStatusRelato map(HistoricoStatusRelatoEntity entity);
}

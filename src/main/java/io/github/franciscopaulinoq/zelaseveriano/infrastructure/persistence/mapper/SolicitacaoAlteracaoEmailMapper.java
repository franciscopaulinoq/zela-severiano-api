package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.SolicitacaoAlteracaoEmail;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.SolicitacaoAlteracaoEmailEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitacaoAlteracaoEmailMapper {
  SolicitacaoAlteracaoEmailEntity map(SolicitacaoAlteracaoEmail solicitacao);

  SolicitacaoAlteracaoEmail map(SolicitacaoAlteracaoEmailEntity entity);
}

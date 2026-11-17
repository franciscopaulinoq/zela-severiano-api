package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RecuperacaoSenha;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RecuperacaoSenhaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecuperacaoSenhaMapper {
  RecuperacaoSenhaEntity map(RecuperacaoSenha recuperacao);

  RecuperacaoSenha map(RecuperacaoSenhaEntity entity);
}

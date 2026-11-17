package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RecuperacaoSenha;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RecuperacaoSenhaRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RecuperacaoSenhaMapper;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecuperacaoSenhaRepositoryImpl implements RecuperacaoSenhaRepository {
  private final JpaRecuperacaoSenhaRepository jpaRepository;
  private final RecuperacaoSenhaMapper mapper;

  @Override
  public RecuperacaoSenha save(RecuperacaoSenha recuperacao) {
    var entity = mapper.map(recuperacao);
    return mapper.map(jpaRepository.saveAndFlush(entity));
  }

  @Override
  public Optional<RecuperacaoSenha> findAtivaPorUsuarioIdECodigoParaAtualizacao(
      UUID usuarioId, String codigo) {
    return jpaRepository
        .findAtivaPorUsuarioIdECodigoParaAtualizacao(usuarioId, codigo)
        .map(mapper::map);
  }

  @Override
  public void invalidarAnteriores(UUID usuarioId, OffsetDateTime momento) {
    jpaRepository.invalidarAnteriores(usuarioId, momento);
  }
}

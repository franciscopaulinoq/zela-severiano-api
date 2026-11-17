package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.SolicitacaoAlteracaoEmail;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.SolicitacaoAlteracaoEmailRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.SolicitacaoAlteracaoEmailMapper;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolicitacaoAlteracaoEmailRepositoryImpl
    implements SolicitacaoAlteracaoEmailRepository {
  private final JpaSolicitacaoAlteracaoEmailRepository jpaRepository;
  private final SolicitacaoAlteracaoEmailMapper mapper;

  @Override
  public SolicitacaoAlteracaoEmail save(SolicitacaoAlteracaoEmail solicitacao) {
    return mapper.map(jpaRepository.save(mapper.map(solicitacao)));
  }

  @Override
  public Optional<SolicitacaoAlteracaoEmail> findByPerfilIdAndCodigoHash(
      UUID perfilId, String codigoHash) {
    return jpaRepository.findValidacao(perfilId, codigoHash).map(mapper::map);
  }

  @Override
  public void revogarAnteriores(UUID perfilId, OffsetDateTime consumidoEm) {
    jpaRepository.revogarAnteriores(perfilId, consumidoEm);
  }
}

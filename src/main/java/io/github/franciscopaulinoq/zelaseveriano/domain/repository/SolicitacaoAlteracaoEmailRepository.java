package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.SolicitacaoAlteracaoEmail;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoAlteracaoEmailRepository {
  SolicitacaoAlteracaoEmail save(SolicitacaoAlteracaoEmail solicitacao);

  Optional<SolicitacaoAlteracaoEmail> findByPerfilIdAndCodigoHash(UUID perfilId, String codigoHash);

  void revogarAnteriores(UUID perfilId, OffsetDateTime consumidoEm);
}

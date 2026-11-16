package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RecuperacaoSenha;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RecuperacaoSenhaRepository {
  RecuperacaoSenha save(RecuperacaoSenha recuperacao);

  Optional<RecuperacaoSenha> findAtivaPorUsuarioIdECodigoParaAtualizacao(
      UUID usuarioId, String codigo);

  void invalidarAnteriores(UUID usuarioId, OffsetDateTime momento);
}

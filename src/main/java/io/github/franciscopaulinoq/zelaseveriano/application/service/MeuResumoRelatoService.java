package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.MeuResumoRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeuResumoRelatoService {

  private final RelatoRepository relatoRepository;
  private final PerfilRepository perfilRepository;

  @Transactional(readOnly = true)
  public MeuResumoRelatoDTO obterResumo(UUID usuarioId) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));
    Long total = relatoRepository.countByPerfilId(perfil.getId());
    return new MeuResumoRelatoDTO(total.intValue());
  }
}

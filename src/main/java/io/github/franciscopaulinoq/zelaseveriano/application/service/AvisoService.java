package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoPublicacaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Aviso;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.AvisoLeitura;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.AvisoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvisoService {

  private final AvisoRepository avisoRepository;
  private final PerfilRepository perfilRepository;

  @Transactional
  public AvisoDTO publicar(AvisoPublicacaoDTO dados) {
    Set<UUID> ids = new LinkedHashSet<>(dados.destinatarios());
    List<Perfil> destinatarios = perfilRepository.findAllById(ids);
    if (destinatarios.size() != ids.size()) {
      throw new RecursoNaoEncontradoException("Um ou mais destinatários não foram encontrados.");
    }

    Aviso aviso = avisoRepository.save(new Aviso(null, dados.titulo(), dados.conteudo(), null));
    avisoRepository.saveAll(
        destinatarios.stream().map(perfil -> new AvisoLeitura(null, aviso, perfil, null)).toList());
    return toDTO(aviso, null);
  }

  @Transactional(readOnly = true)
  public List<AvisoDTO> listarMeus(UUID usuarioId) {
    Perfil perfil = obterPerfil(usuarioId);
    return avisoRepository.findAllByPerfilId(perfil.getId()).stream()
        .map(leitura -> toDTO(leitura.aviso(), leitura.lidaEm()))
        .toList();
  }

  @Transactional
  public void marcarComoLidos(UUID usuarioId, List<Long> avisoIds) {
    Perfil perfil = obterPerfil(usuarioId);
    Set<Long> ids = new LinkedHashSet<>(avisoIds);
    if (ids.isEmpty()) {
      return;
    }

    OffsetDateTime momento = OffsetDateTime.now();
    for (AvisoLeitura leitura :
        avisoRepository.findAllByPerfilIdAndAvisoIdIn(perfil.getId(), ids)) {
      if (leitura.lidaEm() == null) {
        avisoRepository.save(leitura.marcarComoLida(momento));
      }
    }
  }

  private Perfil obterPerfil(UUID usuarioId) {
    return perfilRepository
        .findByUsuarioId(usuarioId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));
  }

  private static AvisoDTO toDTO(Aviso aviso, OffsetDateTime lidaEm) {
    return new AvisoDTO(aviso.id(), aviso.titulo(), aviso.conteudo(), aviso.criadoEm(), lidaEm);
  }
}

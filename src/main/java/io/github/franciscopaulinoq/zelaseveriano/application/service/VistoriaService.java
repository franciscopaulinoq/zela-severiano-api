package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AgendarVistoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.VistoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.ConflitoVistoriaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Vistoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.VistoriaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VistoriaService {
  private final VistoriaRepository vistoriaRepository;
  private final RelatoRepository relatoRepository;
  private final PerfilRepository perfilRepository;

  @Transactional
  public VistoriaDTO agendar(UUID gestorId, Long relatoId, AgendarVistoriaDTO dados) {
    Vistoria vistoria = Vistoria.agendar(relatoId, gestorId, dados.inicio(), dados.fim());
    if (!relatoRepository.existsById(relatoId)) {
      throw new RecursoNaoEncontradoException("Relato não encontrado.");
    }
    if (vistoriaRepository.existeConflito(gestorId, dados.inicio(), dados.fim())) {
      throw new ConflitoVistoriaException();
    }
    return toDTO(vistoriaRepository.save(vistoria));
  }

  @Transactional(readOnly = true)
  public List<VistoriaDTO> listarGestao(Long relatoId) {
    if (!relatoRepository.existsById(relatoId)) {
      throw new RecursoNaoEncontradoException("Relato não encontrado.");
    }
    return listar(relatoId);
  }

  @Transactional
  public void cancelar(Long vistoriaId) {
    Vistoria vistoria =
        vistoriaRepository
            .findById(vistoriaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Vistoria não encontrada."));
    vistoria.cancelar();
    vistoriaRepository.save(vistoria);
  }

  @Transactional(readOnly = true)
  public List<VistoriaDTO> listarDoCidadao(UUID usuarioId, Long relatoId) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));
    if (!relatoRepository.existsByPerfilIdAndId(perfil.getId(), relatoId)) {
      throw new RecursoNaoEncontradoException(
          "Relato não encontrado ou não pertence a este usuário.");
    }
    return listar(relatoId);
  }

  private List<VistoriaDTO> listar(Long relatoId) {
    return vistoriaRepository.findAllByRelatoIdOrderByInicio(relatoId).stream()
        .map(this::toDTO)
        .toList();
  }

  private VistoriaDTO toDTO(Vistoria vistoria) {
    return new VistoriaDTO(
        vistoria.getId(),
        vistoria.getRelatoId(),
        vistoria.getGestorId(),
        vistoria.getInicio(),
        vistoria.getFim(),
        vistoria.getStatus());
  }
}

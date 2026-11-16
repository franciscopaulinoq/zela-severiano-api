package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AuditoriaGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.AuditoriaGestao;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.AuditoriaGestaoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaGestaoService {
  private final AuditoriaGestaoRepository repository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrar(UUID gestorId, String metodo, String caminho, int status) {
    repository.save(
        AuditoriaGestao.builder()
            .gestorId(gestorId)
            .metodo(metodo)
            .caminho(caminho)
            .momento(OffsetDateTime.now())
            .status(status)
            .build());
  }

  @Transactional(readOnly = true)
  public List<AuditoriaGestaoDTO> consultar(UUID gestorId, String metodo) {
    String metodoNormalizado = metodo == null ? null : metodo.toUpperCase(Locale.ROOT);
    return repository.consultar(gestorId, metodoNormalizado).stream().map(this::toDTO).toList();
  }

  private AuditoriaGestaoDTO toDTO(AuditoriaGestao auditoria) {
    return new AuditoriaGestaoDTO(
        auditoria.getId(),
        auditoria.getGestorId(),
        auditoria.getMetodo(),
        auditoria.getCaminho(),
        auditoria.getMomento(),
        auditoria.getStatus());
  }
}

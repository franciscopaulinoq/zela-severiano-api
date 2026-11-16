package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarPerfilDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.SolicitacaoAlteracaoEmail;
import io.github.franciscopaulinoq.zelaseveriano.domain.notification.ConfirmacaoEmailGateway;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.SolicitacaoAlteracaoEmailRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class PerfilService {
  private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(15);

  private final PerfilRepository perfilRepository;
  private final SolicitacaoAlteracaoEmailRepository solicitacaoRepository;
  private final ConfirmacaoEmailGateway emailGateway;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional(readOnly = true)
  public PerfilMeDTO obterMeuPerfil(UUID usuarioId) {
    return toPerfilMeDTO(buscarPerfil(usuarioId));
  }

  @Transactional
  public PerfilMeDTO atualizarMeuPerfil(UUID usuarioId, AtualizarPerfilDTO dados) {
    Perfil perfil = buscarPerfil(usuarioId);
    if (dados.getEmail() != null) {
      perfilRepository.bloquear(perfil.getId());
    }
    perfil.atualizarDados(dados.getNomeCompleto(), dados.getEmail());
    if (dados.getEmail() == null) {
      return toPerfilMeDTO(perfilRepository.save(perfil));
    }

    OffsetDateTime agora = OffsetDateTime.now(clock);
    if (perfil.getEmailPendente() == null) {
      solicitacaoRepository.revogarAnteriores(perfil.getId(), agora);
      return toPerfilMeDTO(perfilRepository.save(perfil));
    }

    String codigo = String.format("%06d", secureRandom.nextInt(1_000_000));
    solicitacaoRepository.save(
        SolicitacaoAlteracaoEmail.builder()
            .id(UUID.randomUUID())
            .perfilId(perfil.getId())
            .emailSolicitado(perfil.getEmailPendente())
            .codigoHash(hash(codigo))
            .criadoEm(agora)
            .expiraEm(agora.plus(VALIDADE_CODIGO))
            .build());
    PerfilMeDTO resultado = toPerfilMeDTO(perfilRepository.save(perfil));
    enviarAposCommit(perfil.getEmailPendente(), codigo);
    return resultado;
  }

  @Transactional
  public PerfilMeDTO confirmarEmail(UUID usuarioId, String codigo) {
    Perfil perfil = buscarPerfil(usuarioId);
    perfilRepository.bloquear(perfil.getId());
    SolicitacaoAlteracaoEmail solicitacao =
        solicitacaoRepository
            .findByPerfilIdAndCodigoHash(perfil.getId(), hash(codigo))
            .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado."));
    OffsetDateTime agora = OffsetDateTime.now(clock);
    if (solicitacao.estaExpirada(agora)) {
      throw new IllegalArgumentException("Código inválido ou expirado.");
    }

    solicitacao.consumir(agora);
    solicitacaoRepository.save(solicitacao);
    perfil.confirmarEmailPendente();
    return toPerfilMeDTO(perfilRepository.save(perfil));
  }

  private void enviarAposCommit(String email, String codigo) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      emailGateway.enviar(email, codigo);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            emailGateway.enviar(email, codigo);
          }
        });
  }

  private String hash(String codigo) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(("confirmacao-email:" + codigo).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponível.", exception);
    }
  }

  private Perfil buscarPerfil(UUID usuarioId) {
    return perfilRepository
        .findByUsuarioId(usuarioId)
        .orElseThrow(() ->
            new RecursoNaoEncontradoException("Perfil não encontrado para o usuário logado."));
  }

  private PerfilMeDTO toPerfilMeDTO(Perfil perfil) {
    return PerfilMeDTO.builder()
        .id(perfil.getId())
        .nomeCompleto(perfil.getNomeCompleto())
        .email(perfil.getEmail())
        .cpf(perfil.getUsuario().getCpf())
        .build();
  }
}

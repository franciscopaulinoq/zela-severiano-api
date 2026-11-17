package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConfirmarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RecuperacaoSenhaSolicitadaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SolicitarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.RecuperacaoSenha;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RecuperacaoSenhaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.RecuperacaoSenhaEmailGateway;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class RecuperacaoSenhaService {

  private static final String RESPOSTA_SOLICITACAO =
      "Se o CPF estiver cadastrado, um código de recuperação será enviado.";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UsuarioRepository usuarioRepository;
  private final PerfilRepository perfilRepository;
  private final RecuperacaoSenhaRepository recuperacaoRepository;
  private final PasswordHasher passwordHasher;
  private final RecuperacaoSenhaEmailGateway emailGateway;
  private final Clock clock;
  private final Duration validade;

  public RecuperacaoSenhaService(
      UsuarioRepository usuarioRepository,
      PerfilRepository perfilRepository,
      RecuperacaoSenhaRepository recuperacaoRepository,
      PasswordHasher passwordHasher,
      RecuperacaoSenhaEmailGateway emailGateway,
      Clock clock,
      @Value("${api.recuperacao.validade:15m}") Duration validade) {
    this.usuarioRepository = usuarioRepository;
    this.perfilRepository = perfilRepository;
    this.recuperacaoRepository = recuperacaoRepository;
    this.passwordHasher = passwordHasher;
    this.emailGateway = emailGateway;
    this.clock = clock;
    this.validade = validade;
  }

  @Transactional
  public RecuperacaoSenhaSolicitadaDTO solicitar(SolicitarRecuperacaoSenhaDTO dados) {
    usuarioRepository.findByCpf(dados.getCpf()).ifPresent(this::emitirCodigo);
    return new RecuperacaoSenhaSolicitadaDTO(RESPOSTA_SOLICITACAO);
  }

  @Transactional
  public void confirmar(ConfirmarRecuperacaoSenhaDTO dados) {
    Usuario usuario = usuarioRepository.findByCpf(dados.getCpf()).orElseThrow(this::codigoInvalido);
    RecuperacaoSenha recuperacao =
        recuperacaoRepository
            .findAtivaPorUsuarioIdECodigoParaAtualizacao(usuario.getId(), dados.getCodigo())
            .orElseThrow(this::codigoInvalido);
    OffsetDateTime agora = OffsetDateTime.now(clock);
    if (recuperacao.expirado(agora)) {
      throw codigoInvalido();
    }

    usuario.alterarSenha(passwordHasher.hash(dados.getNovaSenha()));
    usuarioRepository.save(usuario);
    recuperacao.invalidar(agora);
    recuperacaoRepository.save(recuperacao);
  }

  private void emitirCodigo(Usuario usuario) {
    OffsetDateTime agora = OffsetDateTime.now(clock);
    recuperacaoRepository.invalidarAnteriores(usuario.getId(), agora);
    perfilRepository
        .findByUsuarioId(usuario.getId())
        .map(Perfil::getEmail)
        .filter(email -> !email.isBlank())
        .ifPresent(email -> enviarNovoCodigo(usuario, email, agora));
  }

  private void enviarNovoCodigo(Usuario usuario, String email, OffsetDateTime agora) {
    String codigo = "%06d".formatted(RANDOM.nextInt(1_000_000));
    recuperacaoRepository.save(
        RecuperacaoSenha.builder()
            .id(UUID.randomUUID())
            .usuarioId(usuario.getId())
            .codigo(codigo)
            .expiraEm(agora.plus(validade))
            .criadoEm(agora)
            .build());
    entregarCodigo(email, codigo);
  }

  private void entregarCodigo(String email, String codigo) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      emailGateway.enviar(email, codigo);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            try {
              emailGateway.enviar(email, codigo);
            } catch (RuntimeException exception) {
              log.warn("Falha ao entregar o código de recuperação.", exception);
            }
          }
        });
  }

  private IllegalArgumentException codigoInvalido() {
    return new IllegalArgumentException("Código de recuperação inválido ou expirado.");
  }
}

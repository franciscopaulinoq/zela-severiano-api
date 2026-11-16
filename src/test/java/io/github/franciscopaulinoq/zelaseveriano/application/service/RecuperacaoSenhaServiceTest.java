package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConfirmarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SolicitarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.RecuperacaoSenha;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RecuperacaoSenhaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.RecuperacaoSenhaEmailGateway;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

  private static final Instant AGORA = Instant.parse("2026-03-10T15:00:00Z");
  private static final OffsetDateTime MOMENTO = OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC);

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private RecuperacaoSenhaRepository recuperacaoRepository;
  @Mock private PasswordHasher passwordHasher;
  @Mock private RecuperacaoSenhaEmailGateway emailGateway;

  private RecuperacaoSenhaService service;
  private Usuario usuario;

  @BeforeEach
  void setUp() {
    service =
        new RecuperacaoSenhaService(
            usuarioRepository,
            perfilRepository,
            recuperacaoRepository,
            passwordHasher,
            emailGateway,
            Clock.fixed(AGORA, ZoneOffset.UTC),
            Duration.ofMinutes(15));
    usuario = Usuario.builder().cpf("52998224725").senhaHash("hash-antigo").build();
  }

  @Test
  void solicitacaoParaCpfInexistenteRetornaRespostaGenericaSemEnvio() {
    when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.empty());

    var resposta = service.solicitar(new SolicitarRecuperacaoSenhaDTO(usuario.getCpf()));

    assertThat(resposta.mensagem()).contains("Se o CPF estiver cadastrado");
    verifyNoInteractions(recuperacaoRepository, emailGateway);
  }

  @Test
  void solicitacaoInvalidaAnterioresEEnviaCodigoComValidade() {
    Perfil perfil = Perfil.builder().email("maria@example.com").build();
    when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
    when(perfilRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(perfil));
    when(recuperacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.solicitar(new SolicitarRecuperacaoSenhaDTO(usuario.getCpf()));

    verify(recuperacaoRepository).invalidarAnteriores(usuario.getId(), MOMENTO);
    ArgumentCaptor<RecuperacaoSenha> recuperacao = ArgumentCaptor.forClass(RecuperacaoSenha.class);
    verify(recuperacaoRepository).save(recuperacao.capture());
    assertThat(recuperacao.getValue().getExpiraEm()).isEqualTo(MOMENTO.plusMinutes(15));
    ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
    verify(emailGateway).enviar(eq("maria@example.com"), codigo.capture());
    assertThat(codigo.getValue()).matches("\\d{6}");
  }

  @Test
  void confirmacaoValidaAlteraSenhaEInvalidaCodigo() {
    RecuperacaoSenha recuperacao = recuperacao(MOMENTO.plusMinutes(5));
    when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
    when(recuperacaoRepository.findAtivaPorUsuarioIdECodigoParaAtualizacao(
            eq(usuario.getId()), anyString()))
        .thenReturn(Optional.of(recuperacao));
    when(passwordHasher.hash("nova-senha")).thenReturn("hash-novo");

    service.confirmar(new ConfirmarRecuperacaoSenhaDTO(usuario.getCpf(), "123456", "nova-senha"));

    assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
    assertThat(recuperacao.getInvalidadoEm()).isEqualTo(MOMENTO);
    verify(usuarioRepository).save(usuario);
    verify(recuperacaoRepository).save(recuperacao);
  }

  @Test
  void confirmacaoExpiradaNaoAlteraSenha() {
    RecuperacaoSenha recuperacao = recuperacao(MOMENTO.minusSeconds(1));
    when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
    when(recuperacaoRepository.findAtivaPorUsuarioIdECodigoParaAtualizacao(
            eq(usuario.getId()), anyString()))
        .thenReturn(Optional.of(recuperacao));

    assertThatThrownBy(() ->
            service.confirmar(
                new ConfirmarRecuperacaoSenhaDTO(usuario.getCpf(), "123456", "nova-senha")))
        .isInstanceOf(IllegalArgumentException.class);

    verify(usuarioRepository, never()).save(any());
    verify(recuperacaoRepository, never()).save(any());
    verify(passwordHasher, never()).hash(anyString());
  }

  private RecuperacaoSenha recuperacao(OffsetDateTime expiracao) {
    return RecuperacaoSenha.builder()
        .usuarioId(usuario.getId())
        .codigo("123456")
        .expiraEm(expiracao)
        .build();
  }
}

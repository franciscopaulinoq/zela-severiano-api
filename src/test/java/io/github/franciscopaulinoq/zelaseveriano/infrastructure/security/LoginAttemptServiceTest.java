package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {
  private static final String CPF = "52998224725";
  private static final String OUTRO_CPF = "11144477735";

  @Mock private UsuarioRepository usuarioRepository;
  private RelogioMutavel clock;
  private LoginAttemptService service;

  @BeforeEach
  void setUp() {
    clock = new RelogioMutavel(Instant.parse("2026-01-10T12:00:00Z"));
    service = new LoginAttemptService(usuarioRepository, clock);
    lenient()
        .when(usuarioRepository.findByCpf(CPF))
        .thenReturn(Optional.of(Usuario.builder().cpf(CPF).build()));
    lenient()
        .when(usuarioRepository.findByCpf(OUTRO_CPF))
        .thenReturn(Optional.of(Usuario.builder().cpf(OUTRO_CPF).build()));
  }

  @Test
  void quintaFalhaBloqueiaNovasTentativasDentroDaJanela() {
    for (int tentativa = 1; tentativa < LoginAttemptService.LIMITE_FALHAS; tentativa++) {
      service.registrarFalha(CPF);
      assertThat(service.estaBloqueado(CPF)).isFalse();
    }

    service.registrarFalha(CPF);

    assertThat(service.estaBloqueado(CPF)).isTrue();
  }

  @Test
  void bloqueioExpiraAposQuinzeMinutos() {
    repetirFalhas(CPF, LoginAttemptService.LIMITE_FALHAS);
    clock.avancar(LoginAttemptService.JANELA);

    assertThat(service.estaBloqueado(CPF)).isFalse();
    service.registrarFalha(CPF);
    assertThat(service.estaBloqueado(CPF)).isFalse();
  }

  @Test
  void loginBemSucedidoReiniciaContador() {
    repetirFalhas(CPF, LoginAttemptService.LIMITE_FALHAS - 1);

    service.registrarSucesso(CPF);
    repetirFalhas(CPF, LoginAttemptService.LIMITE_FALHAS - 1);

    assertThat(service.estaBloqueado(CPF)).isFalse();
  }

  @Test
  void contadoresSaoIndependentesPorCpf() {
    repetirFalhas(CPF, LoginAttemptService.LIMITE_FALHAS);

    assertThat(service.estaBloqueado(OUTRO_CPF)).isFalse();
  }

  @Test
  void cpfInvalidoNaoConsultaNemContaTentativa() {
    service.registrarFalha("123");

    assertThat(service.estaBloqueado("123")).isFalse();
    verify(usuarioRepository, never()).findByCpf("123");
  }

  private void repetirFalhas(String cpf, int quantidade) {
    for (int indice = 0; indice < quantidade; indice++) {
      service.registrarFalha(cpf);
    }
  }

  private static final class RelogioMutavel extends Clock {
    private Instant instante;
    private final ZoneId zona;

    private RelogioMutavel(Instant instante) {
      this(instante, ZoneOffset.UTC);
    }

    private RelogioMutavel(Instant instante, ZoneId zona) {
      this.instante = instante;
      this.zona = zona;
    }

    void avancar(Duration duracao) {
      instante = instante.plus(duracao);
    }

    @Override
    public ZoneId getZone() {
      return zona;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return new RelogioMutavel(instante, zone);
    }

    @Override
    public Instant instant() {
      return instante;
    }
  }
}

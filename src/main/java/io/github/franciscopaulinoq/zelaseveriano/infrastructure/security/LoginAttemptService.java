package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Cpf;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
  static final int LIMITE_FALHAS = 5;
  static final Duration JANELA = Duration.ofMinutes(15);
  private static final int INTERVALO_LIMPEZA = 256;

  private final Map<String, JanelaTentativasLogin> tentativas = new ConcurrentHashMap<>();
  private final AtomicInteger registrosDesdeLimpeza = new AtomicInteger();
  private final UsuarioRepository usuarioRepository;
  private final Clock clock;

  public boolean estaBloqueado(String cpf) {
    if (!cpfElegivel(cpf)) {
      return false;
    }

    Instant agora = clock.instant();
    JanelaTentativasLogin janela = tentativas.get(cpf);
    if (janela == null) {
      return false;
    }
    if (janela.expirou(agora, JANELA)) {
      tentativas.remove(cpf, janela);
      return false;
    }
    return janela.estaBloqueada(agora, LIMITE_FALHAS, JANELA);
  }

  public void registrarFalha(String cpf) {
    if (!cpfElegivel(cpf)) {
      return;
    }

    Instant agora = clock.instant();
    tentativas.compute(
        cpf,
        (chave, atual) ->
            atual == null
                ? JanelaTentativasLogin.primeiraFalha(agora)
                : atual.registrarFalha(agora, LIMITE_FALHAS, JANELA));
    if (registrosDesdeLimpeza.incrementAndGet() % INTERVALO_LIMPEZA == 0) {
      tentativas.entrySet().removeIf(entrada -> entrada.getValue().expirou(agora, JANELA));
    }
  }

  public void registrarSucesso(String cpf) {
    if (Cpf.isValido(cpf)) {
      tentativas.remove(cpf);
    }
  }

  private boolean cpfElegivel(String cpf) {
    return Cpf.isValido(cpf) && usuarioRepository.findByCpf(cpf).isPresent();
  }
}

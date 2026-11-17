package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import java.time.Duration;
import java.time.Instant;

final class JanelaTentativasLogin {
  private final Instant inicio;
  private final int falhas;

  JanelaTentativasLogin(Instant inicio, int falhas) {
    this.inicio = inicio;
    this.falhas = falhas;
  }

  static JanelaTentativasLogin primeiraFalha(Instant inicio) {
    return new JanelaTentativasLogin(inicio, 1);
  }

  JanelaTentativasLogin registrarFalha(Instant agora, int limiteFalhas, Duration janela) {
    if (expirou(agora, janela)) {
      return primeiraFalha(agora);
    }
    return new JanelaTentativasLogin(inicio, Math.min(falhas + 1, limiteFalhas));
  }

  boolean estaBloqueada(Instant agora, int limiteFalhas, Duration janela) {
    return falhas >= limiteFalhas && !expirou(agora, janela);
  }

  boolean expirou(Instant agora, Duration janela) {
    return !agora.isBefore(inicio.plus(janela));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

  private final PasswordEncoder passwordEncoder = new PasswordEncoder();

  @Test
  void senhaValidaEhHasheadaEConferida() {
    String hash = passwordEncoder.hash("senha-secreta");

    assertThat(passwordEncoder.matches("senha-secreta", hash)).isTrue();
    assertThat(passwordEncoder.matches("outra-senha", hash)).isFalse();
  }

  @Test
  void senhaComMaisDe72BytesNaoEhHasheada() {
    assertThatThrownBy(() -> passwordEncoder.hash("é".repeat(37)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("72 bytes");
  }

  @Test
  void senhaComMaisDe72BytesNaoConfere() {
    String hash = passwordEncoder.hash("senha-secreta");

    assertThat(passwordEncoder.matches("x".repeat(73), hash)).isFalse();
  }
}

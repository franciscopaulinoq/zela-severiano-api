package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder implements PasswordHasher {

  private static final int TAMANHO_MAXIMO_SENHA_EM_BYTES = 72;

  private final BCryptPasswordEncoder encoder;

  public PasswordEncoder() {
    this.encoder = new BCryptPasswordEncoder(12);
  }

  @Override
  public String hash(String rawPassword) {
    if (excedeLimiteDoBcrypt(rawPassword)) {
      throw new IllegalArgumentException("A senha deve ter no máximo 72 bytes.");
    }
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return !excedeLimiteDoBcrypt(rawPassword) && encoder.matches(rawPassword, encodedPassword);
  }

  private static boolean excedeLimiteDoBcrypt(String rawPassword) {
    return rawPassword != null
        && rawPassword.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_SENHA_EM_BYTES;
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessTokenValidatorTest {

  private static final Instant AGORA = Instant.parse("2026-03-10T15:00:00Z");
  private static final String SEGREDO = "12345678901234567890123456789012";
  private static final UUID USUARIO_ID = UUID.randomUUID();

  private AccessTokenValidator validator;

  @BeforeEach
  void setUp() {
    validator = new AccessTokenValidator(Clock.fixed(AGORA, ZoneOffset.UTC), SEGREDO);
  }

  @Test
  void tokenValidoDevolveUsuarioEPapel() {
    Optional<TokenPayload> payload =
        validator.verificar(token("acesso", AGORA.plus(Duration.ofHours(1))));

    assertThat(payload)
        .hasValueSatisfying(dados -> {
          assertThat(dados.usuarioId()).isEqualTo(USUARIO_ID);
          assertThat(dados.role()).isEqualTo(Role.GESTOR);
        });
  }

  @Test
  void tokenExpiradoEhRecusado() {
    String token = token("acesso", AGORA.minusSeconds(1));

    assertThat(validator.verificar(token)).isEmpty();
  }

  @Test
  void tokenMalformadoEhRecusado() {
    assertThat(validator.verificar("token-malformado")).isEmpty();
  }

  @Test
  void tokenDeRenovacaoEhRecusado() {
    String token = token("renovacao", AGORA.plus(Duration.ofDays(1)));

    assertThat(validator.verificar(token)).isEmpty();
  }

  private String token(String tipo, Instant expiracao) {
    return JWT.create()
        .withIssuer("zela-severiano-api")
        .withSubject(USUARIO_ID.toString())
        .withClaim("role", Role.GESTOR.name())
        .withClaim("tipo", tipo)
        .withExpiresAt(expiracao)
        .sign(Algorithm.HMAC256(SEGREDO));
  }
}

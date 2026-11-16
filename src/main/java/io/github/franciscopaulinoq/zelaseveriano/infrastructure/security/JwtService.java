package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements TokenService {

  private static final String TIPO_RENOVACAO = "renovacao";
  private static final Duration VALIDADE_TOKEN_ACESSO = Duration.ofHours(24);
  private static final Duration VALIDADE_TOKEN_RENOVACAO = Duration.ofDays(7);
  private static final int TAMANHO_MINIMO_SEGREDO_EM_BYTES = 32;

  private final Clock clock;
  private final String secret;
  private final String refreshSecret;
  private final AccessTokenValidator accessTokenValidator;

  public JwtService(
      Clock clock,
      @Value("${api.security.token.secret}") String secret,
      @Value("${api.security.token.refresh-secret}") String refreshSecret,
      AccessTokenValidator accessTokenValidator) {
    validarSegredo(secret, "acesso");
    validarSegredo(refreshSecret, "renovação");
    if (secret.equals(refreshSecret)) {
      throw new IllegalArgumentException("Os segredos dos tokens devem ser diferentes.");
    }
    this.clock = clock;
    this.secret = secret;
    this.refreshSecret = refreshSecret;
    this.accessTokenValidator = accessTokenValidator;
  }

  @Override
  public String gerarToken(Usuario usuario) {
    try {
      return JWT.create()
          .withIssuer(AccessTokenValidator.ISSUER)
          .withSubject(usuario.getId().toString())
          .withClaim(AccessTokenValidator.CLAIM_ROLE, usuario.getRole().name())
          .withClaim(AccessTokenValidator.CLAIM_TIPO, AccessTokenValidator.TIPO_ACESSO)
          .withExpiresAt(clock.instant().plus(VALIDADE_TOKEN_ACESSO))
          .sign(Algorithm.HMAC256(secret));
    } catch (JWTCreationException exception) {
      throw new IllegalStateException("Erro ao gerar token JWT", exception);
    }
  }

  @Override
  public Optional<TokenPayload> validarToken(String token) {
    return accessTokenValidator.verificar(token);
  }

  @Override
  public String gerarTokenDeRenovacao(Usuario usuario) {
    try {
      return JWT.create()
          .withIssuer(AccessTokenValidator.ISSUER)
          .withSubject(usuario.getId().toString())
          .withClaim(AccessTokenValidator.CLAIM_TIPO, TIPO_RENOVACAO)
          .withExpiresAt(clock.instant().plus(VALIDADE_TOKEN_RENOVACAO))
          .sign(Algorithm.HMAC256(refreshSecret));
    } catch (JWTCreationException exception) {
      throw new IllegalStateException("Erro ao gerar token de renovação", exception);
    }
  }

  @Override
  public Optional<UUID> validarTokenDeRenovacao(String token) {
    try {
      DecodedJWT decoded = verificador(refreshSecret).verify(token);
      String subject = decoded.getSubject();
      if (!TIPO_RENOVACAO.equals(decoded.getClaim(AccessTokenValidator.CLAIM_TIPO).asString())
          || subject == null) {
        return Optional.empty();
      }
      return Optional.of(UUID.fromString(subject));
    } catch (JWTVerificationException | IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  private JWTVerifier verificador(String signingSecret) {
    var verification = (JWTVerifier.BaseVerification) JWT.require(Algorithm.HMAC256(signingSecret));
    verification.withIssuer(AccessTokenValidator.ISSUER);
    return verification.build(clock);
  }

  private static void validarSegredo(String secret, String tipo) {
    if (secret == null
        || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO_EM_BYTES) {
      throw new IllegalArgumentException(
          "O segredo do token de %s deve ter pelo menos 32 bytes.".formatted(tipo));
    }
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenValidator {

  static final String ISSUER = "zela-severiano-api";
  static final String CLAIM_ROLE = "role";
  static final String CLAIM_TIPO = "tipo";
  static final String TIPO_ACESSO = "acesso";

  private final JWTVerifier verifier;

  public AccessTokenValidator(Clock clock, @Value("${api.security.token.secret}") String secret) {
    var verification = (JWTVerifier.BaseVerification) JWT.require(Algorithm.HMAC256(secret));
    this.verifier = verification.build(clock);
  }

  public Optional<TokenPayload> verificar(String token) {
    try {
      DecodedJWT decoded = verifier.verify(token);
      String subject = decoded.getSubject();
      String role = decoded.getClaim(CLAIM_ROLE).asString();
      if (!TIPO_ACESSO.equals(decoded.getClaim(CLAIM_TIPO).asString())
          || subject == null
          || role == null) {
        return Optional.empty();
      }
      return Optional.of(new TokenPayload(UUID.fromString(subject), Role.valueOf(role)));
    } catch (JWTVerificationException | IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}

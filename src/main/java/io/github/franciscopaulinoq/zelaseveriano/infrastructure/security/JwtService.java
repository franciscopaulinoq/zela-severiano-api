package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService implements TokenService {

    private static final String CLAIM_ROLE = "role";
    private static final long HORAS_VALIDADE_TOKEN = 24;

    private final Clock clock;

    @Value("${api.security.token.secret}")
    private String secret;

    @Override
    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("zela-severiano-api")
                    .withSubject(usuario.getId().toString())
                    .withClaim(CLAIM_ROLE, usuario.getRole().name())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    @Override
    public Optional<TokenPayload> validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decoded = JWT.require(algorithm)
                    .withIssuer("zela-severiano-api")
                    .build()
                    .verify(token);

            UUID usuarioId = UUID.fromString(decoded.getSubject());
            Role role = Role.valueOf(decoded.getClaim(CLAIM_ROLE).asString());
            return Optional.of(new TokenPayload(usuarioId, role));
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now(clock).plusHours(HORAS_VALIDADE_TOKEN).toInstant(ZoneOffset.UTC);
    }
}

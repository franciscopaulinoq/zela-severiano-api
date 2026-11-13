package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final Instant AGORA = Instant.parse("2026-03-10T15:00:00Z");
    private static final Usuario USUARIO =
            Usuario.builder().cpf("52998224725").role(Role.CIDADAO).build();

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(Clock.fixed(AGORA, ZoneOffset.UTC));
        ReflectionTestUtils.setField(jwtService, "secret", "segredo-de-teste");
    }

    @Test
    void tokenExpiraExatamente24HorasDepoisDaGeracao() {
        String token = jwtService.gerarToken(USUARIO);

        Instant expiracao = JWT.decode(token).getExpiresAtAsInstant();

        assertThat(expiracao).isEqualTo(AGORA.plus(Duration.ofHours(24)));
    }

    @Test
    void tokenGeradoEhValidadoComOUsuarioEOPapel() {
        jwtService = new JwtService(Clock.systemUTC());
        ReflectionTestUtils.setField(jwtService, "secret", "segredo-de-teste");

        Optional<TokenPayload> payload = jwtService.validarToken(jwtService.gerarToken(USUARIO));

        assertThat(payload).hasValueSatisfying(dados -> {
            assertThat(dados.usuarioId()).isEqualTo(USUARIO.getId());
            assertThat(dados.role()).isEqualTo(Role.CIDADAO);
        });
    }
}

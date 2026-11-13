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
import java.util.UUID;
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

    @Test
    void tokenDeRenovacaoValidoDevolveOUsuario() {
        JwtService servico = comRelogio(Clock.systemUTC());

        Optional<UUID> usuarioId = servico.validarTokenDeRenovacao(servico.gerarTokenDeRenovacao(USUARIO));

        assertThat(usuarioId).contains(USUARIO.getId());
    }

    @Test
    void tokenDeRenovacaoExpiradoEhRecusado() {
        JwtService geradoHaOitoDias = comRelogio(Clock.fixed(Instant.now().minus(Duration.ofDays(8)), ZoneOffset.UTC));

        String token = geradoHaOitoDias.gerarTokenDeRenovacao(USUARIO);

        assertThat(comRelogio(Clock.systemUTC()).validarTokenDeRenovacao(token)).isEmpty();
    }

    @Test
    void tokenDeRenovacaoNaoServeComoTokenDeAcesso() {
        JwtService servico = comRelogio(Clock.systemUTC());

        assertThat(servico.validarToken(servico.gerarTokenDeRenovacao(USUARIO))).isEmpty();
    }

    private static JwtService comRelogio(Clock clock) {
        JwtService servico = new JwtService(clock);
        ReflectionTestUtils.setField(servico, "secret", "segredo-de-teste");
        return servico;
    }
}

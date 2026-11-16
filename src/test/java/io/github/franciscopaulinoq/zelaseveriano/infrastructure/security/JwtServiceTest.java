package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final Instant AGORA = Instant.parse("2026-03-10T15:00:00Z");
  private static final String SEGREDO_ACESSO = "12345678901234567890123456789012";
  private static final String SEGREDO_RENOVACAO = "abcdefghijklmnopqrstuvwxyzABCDEF";
  private static final Usuario USUARIO =
      Usuario.builder().cpf("52998224725").role(Role.CIDADAO).build();

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = comRelogio(Clock.fixed(AGORA, ZoneId.of("America/Fortaleza")));
  }

  @Test
  void tokenExpiraExatamente24HorasDepoisDaGeracaoMesmoComRelogioForaDeUtc() {
    String token = jwtService.gerarToken(USUARIO);

    Instant expiracao = JWT.decode(token).getExpiresAtAsInstant();

    assertThat(expiracao).isEqualTo(AGORA.plus(Duration.ofHours(24)));
  }

  @Test
  void tokenGeradoEhValidadoComOUsuarioEOPapel() {
    Optional<TokenPayload> payload = jwtService.validarToken(jwtService.gerarToken(USUARIO));

    assertThat(payload)
        .hasValueSatisfying(dados -> {
          assertThat(dados.usuarioId()).isEqualTo(USUARIO.getId());
          assertThat(dados.role()).isEqualTo(Role.CIDADAO);
        });
  }

  @Test
  void tokenDeRenovacaoValidoDevolveOUsuario() {
    Optional<UUID> usuarioId =
        jwtService.validarTokenDeRenovacao(jwtService.gerarTokenDeRenovacao(USUARIO));

    assertThat(usuarioId).contains(USUARIO.getId());
  }

  @Test
  void tokenDeRenovacaoExpiradoEhRecusado() {
    JwtService geradoHaOitoDias =
        comRelogio(Clock.fixed(AGORA.minus(Duration.ofDays(8)), ZoneOffset.UTC));

    String token = geradoHaOitoDias.gerarTokenDeRenovacao(USUARIO);

    assertThat(jwtService.validarTokenDeRenovacao(token)).isEmpty();
  }

  @Test
  void tokenDeRenovacaoNaoServeComoTokenDeAcesso() {
    assertThat(jwtService.validarToken(jwtService.gerarTokenDeRenovacao(USUARIO))).isEmpty();
  }

  @Test
  void tokenDeAcessoNaoServeParaRenovarASessao() {
    assertThat(jwtService.validarTokenDeRenovacao(jwtService.gerarToken(USUARIO))).isEmpty();
  }

  @Test
  void tokenDeAcessoSemTipoExplicitoEhRecusado() {
    String tokenSemTipo =
        JWT.create()
            .withIssuer("zela-severiano-api")
            .withSubject(USUARIO.getId().toString())
            .withClaim("role", USUARIO.getRole().name())
            .withExpiresAt(AGORA.plus(Duration.ofHours(1)))
            .sign(Algorithm.HMAC256(SEGREDO_ACESSO));

    assertThat(jwtService.validarToken(tokenSemTipo)).isEmpty();
  }

  @Test
  void renovacaoAssinadaComAChaveDeAcessoEhRecusada() {
    String token =
        JWT.create()
            .withIssuer("zela-severiano-api")
            .withSubject(USUARIO.getId().toString())
            .withClaim("tipo", "renovacao")
            .withExpiresAt(AGORA.plus(Duration.ofDays(1)))
            .sign(Algorithm.HMAC256(SEGREDO_ACESSO));

    assertThat(jwtService.validarTokenDeRenovacao(token)).isEmpty();
  }

  @Test
  void segredosIguaisSaoRecusados() {
    Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    assertThatThrownBy(() ->
            new JwtService(
                clock,
                SEGREDO_ACESSO,
                SEGREDO_ACESSO,
                new AccessTokenValidator(clock, SEGREDO_ACESSO)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("devem ser diferentes");
  }

  @Test
  void segredoCurtoEhRecusado() {
    Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    assertThatThrownBy(() ->
            new JwtService(
                clock, "curto", SEGREDO_RENOVACAO, new AccessTokenValidator(clock, SEGREDO_ACESSO)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("32 bytes");
  }

  private static JwtService comRelogio(Clock clock) {
    return new JwtService(
        clock, SEGREDO_ACESSO, SEGREDO_RENOVACAO, new AccessTokenValidator(clock, SEGREDO_ACESSO));
  }
}

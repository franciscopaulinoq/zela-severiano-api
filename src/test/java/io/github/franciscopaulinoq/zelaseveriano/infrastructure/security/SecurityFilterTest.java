package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autenticaComOPapelEOUsuarioDoTokenValido() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        request.addHeader("Authorization", "Bearer token-valido");
        when(jwtService.validarToken("token-valido")).thenReturn(Optional.of(new TokenPayload(usuarioId, Role.GESTOR)));

        securityFilter.doFilter(request, response, filterChain);

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacao).isNotNull();
        assertThat(((UserDetails) autenticacao.getPrincipal()).getUsername()).isEqualTo(usuarioId.toString());
        assertThat(autenticacao.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_GESTOR");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoAutenticaComTokenInvalidoMasSegueACadeia() throws Exception {
        request.addHeader("Authorization", "Bearer token-invalido");
        when(jwtService.validarToken("token-invalido")).thenReturn(Optional.empty());

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoConsultaOTokenQuandoNaoHaCabecalhoDeAutorizacao() throws Exception {
        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }
}

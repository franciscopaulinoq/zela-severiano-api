package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

  private static final String CHAVE = "chave-de-teste";

  @Mock private FilterChain filterChain;

  private final ApiKeyFilter filter = new ApiKeyFilter(CHAVE);
  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();

  @AfterEach
  void limparContexto() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void autenticaComChaveValidaNoCabecalho() throws Exception {
    request.addHeader("X-API-Key", CHAVE);

    filter.doFilter(request, response, filterChain);

    Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
    assertThat(autenticacao).isNotNull();
    assertThat(autenticacao.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_INTEGRACAO");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void naoAutenticaComChaveInvalida() throws Exception {
    request.addHeader("X-API-Key", "incorreta");

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void naoAutenticaSemChave() throws Exception {
    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.franciscopaulinoq.zelaseveriano.application.service.AuditoriaGestaoService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditoriaGestaoFilterTest {
  @Mock private AuditoriaGestaoService auditoriaService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void escritaAdministrativaRegistraGestorRotaEStatus() throws Exception {
    UUID gestorId = UUID.randomUUID();
    autenticar(gestorId, "ROLE_GESTOR");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/v1/gestao/categorias");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(201);

    new AuditoriaGestaoFilter(auditoriaService).doFilter(request, response, new MockFilterChain());

    verify(auditoriaService).registrar(gestorId, "POST", "/api/v1/gestao/categorias", 201);
  }

  @Test
  void consultaNaoGeraEvento() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/gestao/relatos");

    new AuditoriaGestaoFilter(auditoriaService)
        .doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verifyNoInteractions(auditoriaService);
  }

  @Test
  void escritaDeCidadaoNaoEhAtribuidaAGestor() throws Exception {
    autenticar(UUID.randomUUID(), "ROLE_CIDADAO");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/v1/gestao/categorias");

    new AuditoriaGestaoFilter(auditoriaService)
        .doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verifyNoInteractions(auditoriaService);
  }

  private static void autenticar(UUID usuarioId, String papel) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority(papel))));
  }
}

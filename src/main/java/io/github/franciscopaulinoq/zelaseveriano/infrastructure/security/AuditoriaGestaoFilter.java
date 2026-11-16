package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import io.github.franciscopaulinoq.zelaseveriano.application.service.AuditoriaGestaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class AuditoriaGestaoFilter extends OncePerRequestFilter {
  private static final Set<String> ESCRITAS = Set.of("POST", "PUT", "PATCH", "DELETE");
  private static final String PREFIXO_GESTAO = "/api/v1/gestao/";

  private final AuditoriaGestaoService auditoriaService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!ESCRITAS.contains(request.getMethod())
        || !request.getRequestURI().startsWith(PREFIXO_GESTAO)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null
          && authentication.isAuthenticated()
          && authentication.getAuthorities().stream()
              .anyMatch(authority -> "ROLE_GESTOR".equals(authority.getAuthority()))) {
        registrar(request, response, authentication.getName());
      }
    }
  }

  private void registrar(
      HttpServletRequest request, HttpServletResponse response, String gestorId) {
    try {
      auditoriaService.registrar(
          UUID.fromString(gestorId),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus());
    } catch (RuntimeException ex) {
      log.error(
          "Não foi possível registrar auditoria de {} {} com status {}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          ex);
    }
  }
}

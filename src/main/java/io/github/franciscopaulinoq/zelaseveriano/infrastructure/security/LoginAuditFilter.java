package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.security.LoginAuditService.TentativaLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequiredArgsConstructor
public class LoginAuditFilter extends OncePerRequestFilter {

  private final LoginAuditService auditService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, 4096);
    boolean concluida = false;
    try {
      filterChain.doFilter(wrapper, response);
      concluida = true;
    } finally {
      String corpo = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
      TentativaLogin tentativa = auditService.interpretar(corpo);
      if (concluida && response.getStatus() >= 200 && response.getStatus() < 300) {
        auditService.registrarSucesso(tentativa.cpf());
      } else {
        String motivo = response.getStatus() == 400 ? "formato" : "autenticacao";
        auditService.registrarFalha(tentativa, motivo);
      }
    }
  }
}

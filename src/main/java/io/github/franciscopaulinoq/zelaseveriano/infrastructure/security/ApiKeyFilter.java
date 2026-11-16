package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

  private static final String CABECALHO = "X-API-Key";

  private final String chaveEsperada;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String chave = recuperarChave(request);
    if (chaveValida(chave)) {
      var autoridade = new SimpleGrantedAuthority("ROLE_INTEGRACAO");
      var autenticacao =
          new UsernamePasswordAuthenticationToken(
              "integracao-estatisticas", null, List.of(autoridade));
      SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
    filterChain.doFilter(request, response);
  }

  private String recuperarChave(HttpServletRequest request) {
    String chave = request.getHeader(CABECALHO);
    return chave != null ? chave : request.getParameter("apiKey");
  }

  private boolean chaveValida(String chave) {
    return chave != null
        && MessageDigest.isEqual(
            chaveEsperada.getBytes(StandardCharsets.UTF_8), chave.getBytes(StandardCharsets.UTF_8));
  }
}

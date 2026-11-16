package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class LoginThrottlingFilter extends OncePerRequestFilter {
  private final LoginAttemptService loginAttemptService;
  private final ObjectMapper objectMapper;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String caminhoLogin = request.getContextPath() + "/api/v1/auth/login";
    return !"POST".equals(request.getMethod()) || !caminhoLogin.equals(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    RequisicaoLoginWrapper requisicao = new RequisicaoLoginWrapper(request);
    if (requisicao.excedeTamanhoMaximo()) {
      escreverErro(
          response, HttpStatus.BAD_REQUEST, "Requisição inválida", request.getRequestURI());
      return;
    }

    String cpf = extrairCpf(requisicao);
    if (cpf != null && loginAttemptService.estaBloqueado(cpf)) {
      escreverErro(
          response,
          HttpStatus.TOO_MANY_REQUESTS,
          "Muitas tentativas de login. Tente novamente mais tarde.",
          request.getRequestURI());
      return;
    }

    filterChain.doFilter(requisicao, response);
    if (cpf == null) {
      return;
    }
    if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
      loginAttemptService.registrarFalha(cpf);
    } else if (response.getStatus() >= 200 && response.getStatus() < 300) {
      loginAttemptService.registrarSucesso(cpf);
    }
  }

  private String extrairCpf(RequisicaoLoginWrapper requisicao) {
    try {
      JsonNode corpo = objectMapper.readTree(requisicao.conteudo());
      if (corpo == null) {
        return null;
      }
      JsonNode cpf = corpo.path("cpf");
      return cpf.isTextual() ? cpf.asText() : null;
    } catch (JacksonException | IllegalArgumentException exception) {
      return null;
    }
  }

  private void escreverErro(
      HttpServletResponse response, HttpStatus status, String mensagem, String caminho)
      throws IOException {
    Map<String, Object> erro =
        Map.of(
            "timestamp", OffsetDateTime.now(ZoneOffset.UTC),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", mensagem,
            "path", caminho);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), erro);
  }
}

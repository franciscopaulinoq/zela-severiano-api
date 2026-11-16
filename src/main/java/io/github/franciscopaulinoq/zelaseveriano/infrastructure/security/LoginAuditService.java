package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuditService {

  private final ObjectMapper objectMapper;

  public TentativaLogin interpretar(String corpo) {
    try {
      JsonNode json = objectMapper.readTree(corpo);
      if (json == null) {
        return new TentativaLogin(null, null);
      }
      return new TentativaLogin(texto(json, "cpf"), texto(json, "senha"));
    } catch (RuntimeException exception) {
      return new TentativaLogin(null, null);
    }
  }

  public void registrarSucesso(String cpf) {
    log.info(
        "evento=auditoria_login resultado=sucesso momento={} cpf={}", Instant.now(), mascarar(cpf));
  }

  public void registrarFalha(TentativaLogin tentativa, String motivo) {
    String cpf = mascarar(tentativa.cpf());
    log.info(
        "evento=auditoria_login resultado=falha momento={} cpf={} motivo={}",
        Instant.now(),
        cpf,
        motivo);
    log.debug(
        "evento=auditoria_login_detalhe momento={} cpf={} motivo={} senha={}",
        Instant.now(),
        cpf,
        motivo,
        tentativa.senha());
  }

  private String texto(JsonNode json, String campo) {
    JsonNode valor = json.get(campo);
    return valor != null && valor.isTextual() ? valor.asText() : null;
  }

  private String mascarar(String cpf) {
    if (cpf == null) {
      return "nao_informado";
    }
    String digitos = cpf.replaceAll("\\D", "");
    return digitos.length() == 11 ? "***.***." + digitos.substring(6, 9) + "-**" : "invalido";
  }

  public record TentativaLogin(String cpf, String senha) {}
}

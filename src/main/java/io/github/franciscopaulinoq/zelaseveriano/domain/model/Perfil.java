package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Perfil {
  private UUID id;
  private Usuario usuario;
  private String nomeCompleto;
  private String email;
  private String emailPendente;
  private OffsetDateTime criadoEm;

  public void atualizarDados(String novoNomeCompleto, String novoEmail) {
    if (novoNomeCompleto != null) {
      this.nomeCompleto = novoNomeCompleto;
    }
    if (novoEmail != null) {
      String normalizado = novoEmail.strip().toLowerCase(Locale.ROOT);
      this.emailPendente = normalizado.equals(this.email) ? null : normalizado;
    }
  }

  public void confirmarEmailPendente() {
    if (emailPendente == null) {
      throw new IllegalArgumentException("Não há alteração de e-mail pendente.");
    }
    this.email = emailPendente;
    this.emailPendente = null;
  }
}

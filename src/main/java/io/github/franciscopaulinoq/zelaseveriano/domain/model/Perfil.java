package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
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
    private OffsetDateTime criadoEm;

    public void atualizarDados(String novoNomeCompleto, String novoEmail) {
        if (novoNomeCompleto != null) {
            this.nomeCompleto = novoNomeCompleto;
        }
        if (novoEmail != null) {
            this.email = novoEmail;
        }
    }
}

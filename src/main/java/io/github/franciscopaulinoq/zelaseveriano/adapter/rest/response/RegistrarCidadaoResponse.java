package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarCidadaoResponse {
    private UUID usuarioId;
    private UUID perfilId;
    private UUID enderecoId;
    private String cpf;
    private String nomeCompleto;
    private String email;
    private String mensagem;
}

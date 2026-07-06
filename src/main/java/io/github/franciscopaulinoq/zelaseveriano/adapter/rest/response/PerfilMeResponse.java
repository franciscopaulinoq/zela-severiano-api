package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilMeResponse {
    private UUID id;
    private String nomeCompleto;
    private String email;
    private String cpf;
}
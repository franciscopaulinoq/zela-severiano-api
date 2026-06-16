package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class Perfil {
    private UUID id;
    private Usuario usuario;
    private String nomeCompleto;
    private String email;
    private OffsetDateTime criadoEm;
}

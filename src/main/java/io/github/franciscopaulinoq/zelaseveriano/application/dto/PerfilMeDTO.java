package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilMeDTO {
    private UUID id;
    private String nomeCompleto;
    private String email;
    private String cpf;
}
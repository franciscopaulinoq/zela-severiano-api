package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

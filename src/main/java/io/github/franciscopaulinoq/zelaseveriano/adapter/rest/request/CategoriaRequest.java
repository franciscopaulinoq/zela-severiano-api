package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotBlank(message = "Ícone é obrigatório")
    @Size(max = 50, message = "Ícone deve ter no máximo 50 caracteres")
    private String iconeNome;
}

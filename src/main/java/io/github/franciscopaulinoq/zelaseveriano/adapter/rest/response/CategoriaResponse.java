package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponse {
    private Integer id;
    private String nome;
    private String iconeNome;
}
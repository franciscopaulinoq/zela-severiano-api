package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Categoria {
    private Integer id;
    private String nome;
    private String iconeNome;
    private OffsetDateTime criadoEm;
}

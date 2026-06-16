package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Categoria {
    private int id;
    private String nome;
    private String iconeNome;
    private LocalDateTime criadoEm;
}

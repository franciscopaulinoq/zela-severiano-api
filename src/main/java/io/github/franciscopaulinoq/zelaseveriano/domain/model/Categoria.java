package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Categoria {
    private Integer id;
    private String nome;
    private String iconeNome;
    private OffsetDateTime criadoEm;

    public void atualizar(String novoNome, String novoIconeNome) {
        this.nome = novoNome;
        this.iconeNome = novoIconeNome;
    }
}

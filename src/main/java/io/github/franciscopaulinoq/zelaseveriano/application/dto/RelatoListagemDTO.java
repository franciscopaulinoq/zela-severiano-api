package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatoListagemDTO {
    private Long id;
    private String categoriaNome;
    private String categoriaIconeNome;
    private StatusRelato status;
    private OffsetDateTime criadoEm;
}

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
public class RelatoGestaoDTO {
    private Long id;
    private String cidadaoNome;
    private String categoriaNome;
    private StatusRelato status;
    private String descricao;
    private String observacaoResolucao;
    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;
}

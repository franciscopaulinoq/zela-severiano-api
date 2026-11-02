package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
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
public class RelatoGestaoResponse {
    private Long id;
    private String cidadaoNome;
    private String categoriaNome;
    private StatusRelato status;
    private String descricao;
    private String observacaoResolucao;
    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;
}

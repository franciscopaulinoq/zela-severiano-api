package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatoListagemResponse {
    private Long id;
    private String categoriaNome;
    private String categoriaIconeNome;
    private StatusRelato status;
    private OffsetDateTime criadoEm;
}

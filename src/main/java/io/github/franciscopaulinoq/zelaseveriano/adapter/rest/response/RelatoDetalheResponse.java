package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatoDetalheResponse {
    private Long id;
    private String urlFoto;
    private StatusRelato status;
    private OffsetDateTime criadoEm;
    private String categoriaNome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}

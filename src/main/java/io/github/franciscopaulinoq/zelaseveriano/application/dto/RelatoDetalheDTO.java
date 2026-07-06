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
public class RelatoDetalheDTO {
    private Long id;
    private String urlFoto;
    private StatusRelato status;
    private OffsetDateTime criadoEm;
    private String categoriaNome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}

package io.github.franciscopaulinoq.zelaseveriano.application.dto;

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
public class RelatoPublicoDTO {
    private String categoriaNome;
    private String categoriaIconeNome;
    private StatusRelato status;
    private OffsetDateTime criadoEm;
    private Double latitude;
    private Double longitude;
}

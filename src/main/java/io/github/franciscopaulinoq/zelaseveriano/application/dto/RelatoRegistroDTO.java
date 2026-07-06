package io.github.franciscopaulinoq.zelaseveriano.application.dto;

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
public class RelatoRegistroDTO {
    private Integer categoriaId;
    private String descricao;
    private Double latitude;
    private Double longitude;
}

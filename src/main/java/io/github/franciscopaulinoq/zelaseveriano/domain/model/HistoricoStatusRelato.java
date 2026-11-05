package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class HistoricoStatusRelato {
    private Long id;
    private Long relatoId;
    private StatusRelato statusAnterior;
    private StatusRelato statusNovo;
    private String observacao;
    private OffsetDateTime alteradoEm;
}

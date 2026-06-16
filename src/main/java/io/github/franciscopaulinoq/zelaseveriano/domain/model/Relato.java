package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class Relato {
    private int id;
    private UUID perfilId;
    private int categoriaId;
    private StatusRelato status;
    private String descricao;
    private String urlFoto;
    private double latitude;
    private double longitude;
    private LocalDateTime criadoEm;
}

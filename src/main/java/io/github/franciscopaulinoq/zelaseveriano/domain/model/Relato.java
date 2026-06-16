package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Relato {
    private int id;
    private Perfil perfil;
    private Categoria categoria;
    private StatusRelato status;
    private String descricao;
    private String urlFoto;
    private double latitude;
    private double longitude;
    private OffsetDateTime criadoEm;
}

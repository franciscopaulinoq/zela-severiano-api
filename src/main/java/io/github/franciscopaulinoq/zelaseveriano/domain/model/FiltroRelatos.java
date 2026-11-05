package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;

public record FiltroRelatos(StatusRelato status, Integer categoriaId) {

    public static FiltroRelatos semFiltro() {
        return new FiltroRelatos(null, null);
    }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.util.List;
import java.util.Map;

public record EstatisticasRelatosResponse(
        long total, Map<StatusRelato, Long> porStatus, List<TotalPorCategoria> porCategoria) {

    public record TotalPorCategoria(Integer categoriaId, String categoriaNome, long total) {}
}

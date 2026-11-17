package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.util.List;

public record ImportacaoCategoriaResultadoDTO(
    int importadas, List<ImportacaoCategoriaErroDTO> erros) {}

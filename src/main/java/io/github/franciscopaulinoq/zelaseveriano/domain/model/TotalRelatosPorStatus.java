package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;

public record TotalRelatosPorStatus(StatusRelato status, long total) {}

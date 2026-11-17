package io.github.franciscopaulinoq.zelaseveriano.domain.event;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;

public record MudancaStatusRelatoEvent(Long relatoId, String email, StatusRelato novoStatus) {}

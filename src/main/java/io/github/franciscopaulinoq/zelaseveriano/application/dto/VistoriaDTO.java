package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VistoriaDTO(
    Long id,
    Long relatoId,
    UUID gestorId,
    OffsetDateTime inicio,
    OffsetDateTime fim,
    StatusVistoria status) {}

package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditoriaGestaoDTO(
    Long id,
    UUID gestorId,
    String metodo,
    String caminho,
    OffsetDateTime momento,
    Integer status) {}

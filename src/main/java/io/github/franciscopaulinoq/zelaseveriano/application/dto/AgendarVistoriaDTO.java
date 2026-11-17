package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record AgendarVistoriaDTO(
    @NotNull(message = "O início é obrigatório") OffsetDateTime inicio,
    @NotNull(message = "O fim é obrigatório") OffsetDateTime fim) {}

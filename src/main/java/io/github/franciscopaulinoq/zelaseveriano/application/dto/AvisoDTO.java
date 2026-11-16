package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.time.OffsetDateTime;

public record AvisoDTO(
    Long id, String titulo, String conteudo, OffsetDateTime criadoEm, OffsetDateTime lidaEm) {}

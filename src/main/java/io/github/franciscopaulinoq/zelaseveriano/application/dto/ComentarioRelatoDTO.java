package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComentarioRelatoDTO(Long id, UUID autorId, String texto, OffsetDateTime criadoEm) {}

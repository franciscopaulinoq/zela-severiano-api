package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FotoRelatoDTO(Long id, UUID autorId, String urlFoto, OffsetDateTime criadoEm) {}

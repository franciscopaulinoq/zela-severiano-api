package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WebhookDTO(UUID id, String url, boolean ativo, OffsetDateTime criadoEm) {}

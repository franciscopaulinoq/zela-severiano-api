package io.github.franciscopaulinoq.zelaseveriano.domain.security;

import java.util.UUID;

public record TokenRenovacaoPayload(UUID usuarioId, long versaoTokens) {}

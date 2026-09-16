package io.github.franciscopaulinoq.zelaseveriano.domain.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;

import java.util.UUID;

public record TokenPayload(UUID usuarioId, Role role) {
}

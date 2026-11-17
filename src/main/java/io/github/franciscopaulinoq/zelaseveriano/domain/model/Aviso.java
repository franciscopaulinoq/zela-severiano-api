package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.time.OffsetDateTime;

public record Aviso(Long id, String titulo, String conteudo, OffsetDateTime criadoEm) {}

package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import java.util.List;

public record DetalheGestaoRelato(
    Relato relato, List<RelatoFoto> fotos, List<ComentarioRelato> comentarios) {}

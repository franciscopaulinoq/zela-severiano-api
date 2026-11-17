package io.github.franciscopaulinoq.zelaseveriano.application.dto;

public record CategoriaResumoDTO(
    Integer id, String nome, long totalPendentes, boolean possuiPendencias) {}

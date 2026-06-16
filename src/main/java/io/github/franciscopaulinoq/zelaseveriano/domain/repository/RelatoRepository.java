package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;

import java.util.Optional;
import java.util.UUID;

public interface RelatoRepository {
    public Relato save(Relato relato);

    public Optional<Relato> findByPerfilIdAndId(UUID perfilId, int relatoId);
}

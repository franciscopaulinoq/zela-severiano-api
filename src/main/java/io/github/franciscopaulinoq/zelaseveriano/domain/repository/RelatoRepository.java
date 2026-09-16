package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelatoRepository {
    Relato save(Relato relato);
    Optional<Relato> findByPerfilIdAndId(UUID perfilId, Long relatoId);
    Optional<Relato> findById(Long relatoId);
    List<Relato> findAllByPerfilId(UUID perfilId);
    List<Relato> findAll();
}

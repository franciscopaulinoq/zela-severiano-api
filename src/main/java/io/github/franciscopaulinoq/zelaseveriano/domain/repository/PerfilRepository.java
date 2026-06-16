package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;

import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository {
    public Perfil save(Perfil perfil);

    public Optional<Perfil> findById(UUID perfilId);

    public Optional<Perfil> findByUserId(UUID usuarioId);
}

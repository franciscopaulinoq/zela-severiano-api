package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;

import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository {
    Perfil save(Perfil perfil);

    Optional<Perfil> findById(UUID perfilId);

    Optional<Perfil> findByUsuarioId(UUID usuarioId);
}

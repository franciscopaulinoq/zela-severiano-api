package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID usuarioId);

    Optional<Usuario> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}

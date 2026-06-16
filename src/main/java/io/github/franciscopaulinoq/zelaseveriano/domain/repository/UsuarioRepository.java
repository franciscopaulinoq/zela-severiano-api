package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    public Usuario save(Usuario usuario);

    public Optional<Usuario> findById(UUID usuarioId);

    public Optional<Usuario> findByCpf(String cpf);

    public boolean existsByCpf(String cpf);
}

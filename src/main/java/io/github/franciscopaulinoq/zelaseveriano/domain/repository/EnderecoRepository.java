package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;

import java.util.Optional;
import java.util.UUID;

public interface EnderecoRepository {
    public Endereco save(Endereco endereco);

    public Optional<Endereco> findById(UUID enderecoId);

    Optional<Endereco> findByPerfilId(UUID perfilId);
}

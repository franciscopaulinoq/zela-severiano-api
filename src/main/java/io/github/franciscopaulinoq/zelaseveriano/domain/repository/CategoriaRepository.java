package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    Optional<Categoria> findById(Integer categoriaId);

    List<Categoria> findAll();

    boolean existsById(Integer categoriaId);
}

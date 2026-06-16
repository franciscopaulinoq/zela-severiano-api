package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    public Optional<Categoria> findById(Long categoriaId);

    public List<Categoria> findAll();

    public boolean existsById(Long categoriaId);
}

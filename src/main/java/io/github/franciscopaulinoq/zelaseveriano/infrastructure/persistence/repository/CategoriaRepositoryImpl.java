package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final JpaCategoriaRepository jpaRepository;
    private final CategoriaMapper mapper;

    @Override
    public Optional<Categoria> findById(Long categoriaId) {
        return jpaRepository.findById(categoriaId)
                .map(mapper::map);
    }

    @Override
    public List<Categoria> findAll() {
        return jpaRepository.findAll(Sort.by("id")).stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long categoriaId) {
        return jpaRepository.existsById(categoriaId);
    }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaJaRegistradaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.CategoriaMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryImpl implements CategoriaRepository {
  private final JpaCategoriaRepository jpaRepository;
  private final CategoriaMapper mapper;

  @Override
  public Optional<Categoria> findById(Integer categoriaId) {
    return jpaRepository.findById(categoriaId).map(mapper::map);
  }

  @Override
  public List<Categoria> findAll() {
    return jpaRepository.findAll(Sort.by("id")).stream()
        .map(mapper::map)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsById(Integer categoriaId) {
    return jpaRepository.existsById(categoriaId);
  }

  @Override
  public boolean existsByNomeNormalizado(String nomeNormalizado) {
    return jpaRepository.existsByNomeNormalizado(nomeNormalizado);
  }

  @Override
  public boolean existsByNomeNormalizadoAndIdNot(String nomeNormalizado, Integer categoriaId) {
    return jpaRepository.existsByNomeNormalizadoAndIdNot(nomeNormalizado, categoriaId);
  }

  @Override
  public Categoria save(Categoria categoria) {
    try {
      return mapper.map(jpaRepository.saveAndFlush(mapper.map(categoria)));
    } catch (DataIntegrityViolationException ex) {
      if (violouNomeNormalizado(ex)) {
        throw new CategoriaJaRegistradaException();
      }
      throw ex;
    }
  }

  private boolean violouNomeNormalizado(DataIntegrityViolationException ex) {
    for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
      if (causa instanceof ConstraintViolationException violacao
          && "categorias_nome_normalizado_uk".equals(violacao.getConstraintName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void deleteById(Integer categoriaId) {
    jpaRepository.deleteById(categoriaId);
  }
}

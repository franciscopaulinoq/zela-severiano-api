package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Vistoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.VistoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.VistoriaMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VistoriaRepositoryImpl implements VistoriaRepository {
  private final JpaVistoriaRepository jpaRepository;
  private final VistoriaMapper mapper;

  @Override
  public Vistoria save(Vistoria vistoria) {
    return mapper.map(jpaRepository.saveAndFlush(mapper.map(vistoria)));
  }

  @Override
  public Optional<Vistoria> findById(Long vistoriaId) {
    return jpaRepository.findById(vistoriaId).map(mapper::map);
  }

  @Override
  public boolean existeConflito(UUID gestorId, OffsetDateTime inicio, OffsetDateTime fim) {
    return jpaRepository.existsByGestorIdAndStatusAndInicioLessThanAndFimGreaterThan(
        gestorId, StatusVistoria.ATIVA, fim, inicio);
  }

  @Override
  public List<Vistoria> findAllByRelatoIdOrderByInicio(Long relatoId) {
    return jpaRepository.findAllByRelatoIdOrderByInicio(relatoId).stream()
        .map(mapper::map)
        .toList();
  }
}

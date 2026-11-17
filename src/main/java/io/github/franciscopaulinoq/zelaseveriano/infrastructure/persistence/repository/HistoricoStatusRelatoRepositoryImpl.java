package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.HistoricoStatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.HistoricoStatusRelatoMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoricoStatusRelatoRepositoryImpl implements HistoricoStatusRelatoRepository {
  private final JpaHistoricoStatusRelatoRepository jpaRepository;
  private final HistoricoStatusRelatoMapper mapper;

  @Override
  public HistoricoStatusRelato save(HistoricoStatusRelato historico) {
    return mapper.map(jpaRepository.save(mapper.map(historico)));
  }

  @Override
  public List<HistoricoStatusRelato> findAllByRelatoId(Long relatoId) {
    return jpaRepository.findAllByRelatoIdOrderByAlteradoEmAscIdAsc(relatoId).stream()
        .map(mapper::map)
        .toList();
  }

  @Override
  public List<HistoricoStatusRelato> findAfterId(long id, int limite) {
    return jpaRepository.findAllByIdGreaterThanOrderByIdAsc(id, PageRequest.of(0, limite)).stream()
        .map(mapper::map)
        .toList();
  }

  @Override
  public long maxId() {
    return jpaRepository.maxId();
  }
}

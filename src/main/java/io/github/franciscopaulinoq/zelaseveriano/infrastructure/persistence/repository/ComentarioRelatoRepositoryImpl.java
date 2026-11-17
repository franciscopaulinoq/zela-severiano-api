package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.ComentarioRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.ComentarioRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.ComentarioRelatoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.ComentarioRelatoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ComentarioRelatoRepositoryImpl implements ComentarioRelatoRepository {
  private final JpaComentarioRelatoRepository jpaRepository;
  private final ComentarioRelatoMapper mapper;

  @Override
  public ComentarioRelato save(ComentarioRelato comentario) {
    ComentarioRelatoEntity entity = mapper.map(comentario);
    entity.setRelato(RelatoEntity.builder().id(comentario.getRelatoId()).build());
    return mapper.map(jpaRepository.save(entity));
  }
}

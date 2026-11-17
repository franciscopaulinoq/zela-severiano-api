package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.RelatoFoto;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoFotoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoFotoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RelatoFotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RelatoFotoRepositoryImpl implements RelatoFotoRepository {
  private final JpaRelatoFotoRepository jpaRepository;
  private final RelatoFotoMapper mapper;

  @Override
  public RelatoFoto save(RelatoFoto foto) {
    RelatoFotoEntity entity = mapper.map(foto);
    entity.setRelato(RelatoEntity.builder().id(foto.getRelatoId()).build());
    return mapper.map(jpaRepository.save(entity));
  }

  @Override
  public long countByRelatoId(Long relatoId) {
    return jpaRepository.countByRelatoId(relatoId);
  }
}

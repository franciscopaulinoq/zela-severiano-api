package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.DetalheGestaoRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoDetalheRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.ComentarioRelatoMapper;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RelatoFotoMapper;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RelatoMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RelatoDetalheRepositoryImpl implements RelatoDetalheRepository {
  private final JpaRelatoDetalheRepository jpaRepository;
  private final RelatoMapper relatoMapper;
  private final RelatoFotoMapper fotoMapper;
  private final ComentarioRelatoMapper comentarioMapper;

  @Override
  public Optional<DetalheGestaoRelato> buscarDetalheCompleto(Long relatoId) {
    return jpaRepository.buscarDetalheCompleto(relatoId).stream()
        .findFirst()
        .map(entity ->
            new DetalheGestaoRelato(
                relatoMapper.map(entity),
                entity.getFotos().stream().map(fotoMapper::map).toList(),
                entity.getComentarios().stream().map(comentarioMapper::map).toList()));
  }
}

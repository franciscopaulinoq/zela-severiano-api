package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelatoRepository {
  Relato save(Relato relato);

  int atualizarCategoriaEmLote(
      Set<Long> relatoIds, Integer categoriaId, OffsetDateTime atualizadoEm);

  Optional<Relato> findByPerfilIdAndId(UUID perfilId, Long relatoId);

  Optional<Relato> findById(Long relatoId);

  List<Relato> findAllById(Set<Long> relatoIds);

  Optional<Relato> findByIdParaAtualizacao(Long relatoId);

  Optional<Relato> findByPerfilIdAndIdParaAtualizacao(UUID perfilId, Long relatoId);

  Page<Relato> findAllByPerfilId(UUID perfilId, FiltroRelatos filtro, Pageable pageable);

  Page<Relato> findAll(FiltroRelatos filtro, Pageable pageable);

  long countByPerfilIdAndStatus(UUID perfilId, StatusRelato status);

  boolean existsByCategoriaId(Integer categoriaId);

  long count();

  List<TotalRelatosPorStatus> contarPorStatus();

  List<TotalRelatosPorCategoria> contarPorCategoria();
}

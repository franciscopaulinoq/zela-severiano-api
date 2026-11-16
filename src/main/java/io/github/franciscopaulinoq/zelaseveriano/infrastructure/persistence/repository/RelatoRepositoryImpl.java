package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RelatoMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RelatoRepositoryImpl implements RelatoRepository {
  private static final Sort MAIS_RECENTES_PRIMEIRO =
      Sort.by(Sort.Direction.DESC, "criadoEm").and(Sort.by(Sort.Direction.DESC, "id"));

  private final JpaRelatoRepository jpaRepository;
  private final RelatoMapper mapper;

  @Override
  public Relato save(Relato relato) {
    RelatoEntity entity = mapper.map(relato);
    RelatoEntity saved = jpaRepository.save(entity);
    return mapper.map(saved);
  }

  @Override
  public List<Relato> saveAll(List<Relato> relatos) {
    List<RelatoEntity> entidades = relatos.stream().map(mapper::map).toList();
    return jpaRepository.saveAll(entidades).stream().map(mapper::map).toList();
  }

  @Override
  public Optional<Relato> findByIdentificadorExterno(String identificadorExterno) {
    return jpaRepository.findByIdentificadorExterno(identificadorExterno).map(mapper::map);
  }

  @Override
  public Optional<Relato> findByPerfilIdAndId(UUID perfilId, Long relatoId) {
    return jpaRepository.findByPerfilIdAndId(perfilId, relatoId).map(mapper::map);
  }

  @Override
  public Optional<Relato> findById(Long relatoId) {
    return jpaRepository.findById(relatoId).map(mapper::map);
  }

  @Override
  public Optional<Relato> findByIdParaAtualizacao(Long relatoId) {
    return jpaRepository.findByIdParaAtualizacao(relatoId).map(mapper::map);
  }

  @Override
  public Optional<Relato> findByPerfilIdAndIdParaAtualizacao(UUID perfilId, Long relatoId) {
    return jpaRepository.findByPerfilIdAndIdParaAtualizacao(perfilId, relatoId).map(mapper::map);
  }

  @Override
  public Page<Relato> findAllByPerfilId(UUID perfilId, FiltroRelatos filtro, Pageable pageable) {
    return jpaRepository
        .findAll(
            RelatoSpecifications.doPerfil(perfilId).and(RelatoSpecifications.comFiltro(filtro)),
            paginar(pageable))
        .map(mapper::map);
  }

  @Override
  public Page<Relato> findAll(FiltroRelatos filtro, Pageable pageable) {
    return jpaRepository
        .findAll(RelatoSpecifications.comFiltro(filtro), paginar(pageable))
        .map(mapper::map);
  }

  @Override
  public long countByPerfilIdAndStatus(UUID perfilId, StatusRelato status) {
    return jpaRepository.countByPerfilIdAndStatus(perfilId, status);
  }

  @Override
  public boolean existsByCategoriaId(Integer categoriaId) {
    return jpaRepository.existsByCategoriaId(categoriaId);
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }

  @Override
  public List<TotalRelatosPorStatus> contarPorStatus() {
    return jpaRepository.contarPorStatus();
  }

  @Override
  public List<TotalRelatosPorCategoria> contarPorCategoria() {
    return jpaRepository.contarPorCategoria();
  }

  private static PageRequest paginar(Pageable pageable) {
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), MAIS_RECENTES_PRIMEIRO);
  }
}

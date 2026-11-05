package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
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
    private final JpaRelatoRepository jpaRepository;
    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final RelatoMapper mapper;

    @Override
    public Relato save(Relato relato) {
        RelatoEntity entity = mapper.map(relato);
        RelatoEntity saved = jpaRepository.save(entity);
        return mapper.map(saved);
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
    public Page<Relato> findAllByPerfilId(UUID perfilId, FiltroRelatos filtro, Pageable pageable) {
        PageRequest pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), MAIS_RECENTES_PRIMEIRO);
        return jpaRepository
                .findAll(
                        RelatoSpecifications.doPerfil(perfilId).and(RelatoSpecifications.comFiltro(filtro)),
                        pageRequest)
                .map(mapper::map);
    }

    @Override
    public List<Relato> findAll(FiltroRelatos filtro) {
        return jpaRepository.findAll(RelatoSpecifications.comFiltro(filtro), MAIS_RECENTES_PRIMEIRO).stream()
                .map(mapper::map)
                .toList();
    }
}

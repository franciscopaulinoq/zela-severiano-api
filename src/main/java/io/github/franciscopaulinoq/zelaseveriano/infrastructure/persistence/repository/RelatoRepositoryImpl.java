package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.RelatoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RelatoRepositoryImpl implements RelatoRepository {
    private final JpaRelatoRepository jpaRepository;
    private final RelatoMapper mapper;

    @Override
    public Relato save(Relato relato) {
        RelatoEntity entity = mapper.map(relato);
        RelatoEntity saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<Relato> findByPerfilIdAndId(UUID perfilId, Long relatoId) {
        return jpaRepository.findByPerfilIdAndId(perfilId, relatoId)
                .map(mapper::map);
    }

    @Override
    public Optional<Relato> findById(Long relatoId) {
        return jpaRepository.findById(relatoId)
                .map(mapper::map);
    }

    @Override
    public List<Relato> findAllByPerfilId(UUID perfilId) {
        return jpaRepository.findAllByPerfilIdOrderByCriadoEmDesc(perfilId).stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<Relato> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }
}
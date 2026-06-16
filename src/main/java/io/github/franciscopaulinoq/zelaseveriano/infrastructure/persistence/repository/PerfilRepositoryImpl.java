package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.PerfilEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.PerfilMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PerfilRepositoryImpl implements PerfilRepository {
    private final JpaPerfilRepository jpaRepository;
    private final PerfilMapper mapper;

    @Override
    public Perfil save(Perfil perfil) {
        PerfilEntity entity = mapper.map(perfil);
        PerfilEntity saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<Perfil> findById(UUID perfilId) {
        return jpaRepository.findById(perfilId)
                .map(mapper::map);
    }

    @Override
    public Optional<Perfil> findByUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId)
                .map(mapper::map);
    }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.UsuarioEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final JpaUsuarioRepository jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.map(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.map(savedEntity);
    }

    @Override
    public Optional<Usuario> findById(UUID usuarioId) {
        return jpaRepository.findById(usuarioId)
                .map(mapper::map);
    }

    @Override
    public Optional<Usuario> findByCpf(String cpf) {
        return jpaRepository.findByCpf(cpf)
                .map(mapper::map);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }
}

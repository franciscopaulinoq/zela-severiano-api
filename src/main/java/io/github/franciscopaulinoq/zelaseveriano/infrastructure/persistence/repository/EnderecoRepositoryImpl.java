package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EnderecoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EnderecoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.EnderecoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EnderecoRepositoryImpl implements EnderecoRepository {
    private final JpaEnderecoRepository jpaRepository;
    private final EnderecoMapper mapper;

    @Override
    public Endereco save(Endereco endereco) {
        EnderecoEntity entity = mapper.map(endereco);
        EnderecoEntity saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<Endereco> findById(UUID enderecoId) {
        return jpaRepository.findById(enderecoId)
                .map(mapper::map);
    }

    @Override
    public Optional<Endereco> findByPerfilId(UUID perfilId) {
        return jpaRepository.findByPerfilId(perfilId)
                .map(mapper::map);
    }
}

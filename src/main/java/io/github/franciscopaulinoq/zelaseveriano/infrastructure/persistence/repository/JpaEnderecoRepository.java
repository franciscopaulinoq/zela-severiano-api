package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.EnderecoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEnderecoRepository extends JpaRepository<EnderecoEntity, UUID> {
    Optional<EnderecoEntity> findByPerfilId(UUID perfilId);
}

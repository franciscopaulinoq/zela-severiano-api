package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.ComentarioRelatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaComentarioRelatoRepository
    extends JpaRepository<ComentarioRelatoEntity, Long> {}

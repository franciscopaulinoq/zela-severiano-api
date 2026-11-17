package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AvisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAvisoRepository extends JpaRepository<AvisoEntity, Long> {}

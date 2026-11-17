package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AuditoriaGestaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaAuditoriaGestaoRepository
    extends JpaRepository<AuditoriaGestaoEntity, Long>,
        JpaSpecificationExecutor<AuditoriaGestaoEntity> {}

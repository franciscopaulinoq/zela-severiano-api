package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.RelatoEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class RelatoSpecifications {

    private RelatoSpecifications() {}

    static Specification<RelatoEntity> doPerfil(UUID perfilId) {
        return (root, query, cb) -> cb.equal(root.get("perfil").get("id"), perfilId);
    }

    static Specification<RelatoEntity> comFiltro(FiltroRelatos filtro) {
        return (root, query, cb) -> {
            var condicoes = cb.conjunction();
            if (filtro.status() != null) {
                condicoes = cb.and(condicoes, cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.categoriaId() != null) {
                condicoes = cb.and(condicoes, cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            return condicoes;
        };
    }
}

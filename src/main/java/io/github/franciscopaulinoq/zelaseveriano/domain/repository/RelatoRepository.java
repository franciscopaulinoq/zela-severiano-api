package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelatoRepository {
    Relato save(Relato relato);

    Optional<Relato> findByPerfilIdAndId(UUID perfilId, Long relatoId);

    Optional<Relato> findById(Long relatoId);

    Page<Relato> findAllByPerfilId(UUID perfilId, FiltroRelatos filtro, Pageable pageable);

    List<Relato> findAll(FiltroRelatos filtro);
}

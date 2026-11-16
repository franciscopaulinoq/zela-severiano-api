package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AvisoLeituraEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAvisoLeituraRepository extends JpaRepository<AvisoLeituraEntity, Long> {
  @EntityGraph(attributePaths = {"aviso", "perfil", "perfil.usuario"})
  List<AvisoLeituraEntity> findAllByPerfilIdOrderByAvisoCriadoEmDesc(UUID perfilId);

  @EntityGraph(attributePaths = {"aviso", "perfil", "perfil.usuario"})
  List<AvisoLeituraEntity> findAllByPerfilIdAndAvisoIdIn(UUID perfilId, Collection<Long> avisoIds);
}

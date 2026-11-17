package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository {
  Perfil save(Perfil perfil);

  Optional<Perfil> findById(UUID perfilId);

  List<Perfil> findAllById(Collection<UUID> perfilIds);

  Optional<Perfil> findByUsuarioId(UUID usuarioId);

  void bloquear(UUID perfilId);
}

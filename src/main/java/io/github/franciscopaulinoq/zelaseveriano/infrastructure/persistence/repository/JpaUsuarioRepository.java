package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.UsuarioEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
  Optional<UsuarioEntity> findByCpf(String cpf);

  boolean existsByCpf(String cpf);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UsuarioEntity u where u.id = :usuarioId")
  Optional<UsuarioEntity> findByIdParaAtualizacao(@Param("usuarioId") UUID usuarioId);
}

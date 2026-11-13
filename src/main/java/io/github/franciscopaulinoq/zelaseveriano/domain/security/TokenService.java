package io.github.franciscopaulinoq.zelaseveriano.domain.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface TokenService {
    String gerarToken(Usuario usuario);

    Optional<TokenPayload> validarToken(String token);

    String gerarTokenDeRenovacao(Usuario usuario);

    Optional<UUID> validarTokenDeRenovacao(String token);
}

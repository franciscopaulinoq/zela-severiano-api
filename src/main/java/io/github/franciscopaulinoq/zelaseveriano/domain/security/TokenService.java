package io.github.franciscopaulinoq.zelaseveriano.domain.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;

public interface TokenService {
    String gerarToken(Usuario usuario);
    String validarToken(String token);
}
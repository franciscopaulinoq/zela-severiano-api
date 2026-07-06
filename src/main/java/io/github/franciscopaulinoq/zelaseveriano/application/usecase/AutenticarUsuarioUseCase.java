package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public String execute(String cpf, String senha) {
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("CPF ou senha inválidos"));

        if (!passwordHasher.matches(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("CPF ou senha inválidos");
        }

        return tokenService.gerarToken(usuario);
    }
}
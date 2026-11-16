package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class GestorDesenvolvimentoSeeder implements ApplicationRunner {

  private static final String CPF = "11144477735";
  private static final String SENHA = "gestor123";

  private final UsuarioRepository usuarioRepository;
  private final PerfilRepository perfilRepository;
  private final PasswordHasher passwordHasher;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (usuarioRepository.existsByCpf(CPF)) {
      return;
    }

    Usuario gestor =
        usuarioRepository.save(
            Usuario.builder()
                .cpf(CPF)
                .senhaHash(passwordHasher.hash(SENHA))
                .role(Role.GESTOR)
                .build());
    perfilRepository.save(
        Perfil.builder()
            .id(UUID.randomUUID())
            .usuario(gestor)
            .nomeCompleto("Gestor Local")
            .email("gestor@zelaseveriano.local")
            .build());
  }
}

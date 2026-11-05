package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarPerfilDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    private static final UUID USUARIO_ID = UUID.randomUUID();

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private PerfilService perfilService;

    @Test
    void retornaOPerfilDoUsuarioAutenticado() {
        Usuario usuario = Usuario.builder()
                .id(USUARIO_ID)
                .cpf("52998224725")
                .senhaHash("hash")
                .build();
        Perfil perfil = Perfil.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nomeCompleto("Maria da Silva")
                .email("maria@example.com")
                .build();
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));

        PerfilMeDTO resultado = perfilService.obterMeuPerfil(USUARIO_ID);

        assertThat(resultado.getId()).isEqualTo(perfil.getId());
        assertThat(resultado.getNomeCompleto()).isEqualTo("Maria da Silva");
        assertThat(resultado.getEmail()).isEqualTo("maria@example.com");
        assertThat(resultado.getCpf()).isEqualTo("52998224725");
    }

    @Test
    void recusaUsuarioSemPerfil() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.obterMeuPerfil(USUARIO_ID)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void atualizaSoONomeQuandoSoONomeEhInformado() {
        Perfil perfil = perfilExistente();

        PerfilMeDTO resultado =
                perfilService.atualizarMeuPerfil(USUARIO_ID, new AtualizarPerfilDTO("Maria Souza", null));

        assertThat(resultado.getNomeCompleto()).isEqualTo("Maria Souza");
        assertThat(resultado.getEmail()).isEqualTo("maria@example.com");
        assertThat(perfil.getNomeCompleto()).isEqualTo("Maria Souza");
    }

    @Test
    void atualizaSoOEmailQuandoSoOEmailEhInformado() {
        perfilExistente();

        PerfilMeDTO resultado =
                perfilService.atualizarMeuPerfil(USUARIO_ID, new AtualizarPerfilDTO(null, "maria.souza@example.com"));

        assertThat(resultado.getNomeCompleto()).isEqualTo("Maria da Silva");
        assertThat(resultado.getEmail()).isEqualTo("maria.souza@example.com");
    }

    @Test
    void atualizaNomeEEmailJuntos() {
        perfilExistente();

        PerfilMeDTO resultado = perfilService.atualizarMeuPerfil(
                USUARIO_ID, new AtualizarPerfilDTO("Maria Souza", "maria.souza@example.com"));

        assertThat(resultado.getNomeCompleto()).isEqualTo("Maria Souza");
        assertThat(resultado.getEmail()).isEqualTo("maria.souza@example.com");
    }

    private Perfil perfilExistente() {
        Usuario usuario = Usuario.builder().id(USUARIO_ID).cpf("52998224725").build();
        Perfil perfil = Perfil.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nomeCompleto("Maria da Silva")
                .email("maria@example.com")
                .build();
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        return perfil;
    }
}

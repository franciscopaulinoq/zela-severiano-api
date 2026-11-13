package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EnderecoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    private static final String CPF = "52998224725";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ContaService contaService;

    @Test
    void recusaCadastroDeCpfJaRegistradoSemPersistirNada() {
        when(usuarioRepository.existsByCpf(CPF)).thenReturn(true);

        assertThatThrownBy(() -> contaService.registrarCidadao(novoCidadao(), "senha-secreta"))
                .isInstanceOf(CidadaoJaRegistradoException.class);

        verify(passwordHasher, never()).hash(anyString());
        verify(usuarioRepository, never()).save(any());
        verify(perfilRepository, never()).save(any());
        verify(enderecoRepository, never()).save(any());
    }

    @Test
    void cadastraCidadaoComSenhaHasheadaPapelCidadaoEEnderecoNoMunicipio() {
        when(usuarioRepository.existsByCpf(CPF)).thenReturn(false);
        when(passwordHasher.hash("senha-secreta")).thenReturn("hash-da-senha");
        when(usuarioRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(perfilRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(enderecoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        RegistrarCidadaoDTO resultado = contaService.registrarCidadao(novoCidadao(), "senha-secreta");

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuario.capture());
        assertThat(usuario.getValue().getCpf()).isEqualTo(CPF);
        assertThat(usuario.getValue().getSenhaHash()).isEqualTo("hash-da-senha");
        assertThat(usuario.getValue().getRole()).isEqualTo(Role.CIDADAO);

        ArgumentCaptor<Perfil> perfil = ArgumentCaptor.forClass(Perfil.class);
        verify(perfilRepository).save(perfil.capture());
        assertThat(perfil.getValue().getUsuario()).isSameAs(usuario.getValue());
        assertThat(perfil.getValue().getNomeCompleto()).isEqualTo("Maria da Silva");

        ArgumentCaptor<Endereco> endereco = ArgumentCaptor.forClass(Endereco.class);
        verify(enderecoRepository).save(endereco.capture());
        assertThat(endereco.getValue().getPerfil()).isSameAs(perfil.getValue());
        assertThat(endereco.getValue().getCidade()).isEqualTo("Doutor Severiano");
        assertThat(endereco.getValue().getUf()).isEqualTo("RN");
        assertThat(endereco.getValue().getCep()).isEqualTo("59910000");
        assertThat(endereco.getValue().getBairro()).isEqualTo("Centro");

        assertThat(resultado.getUsuarioId()).isEqualTo(usuario.getValue().getId());
        assertThat(resultado.getPerfilId()).isEqualTo(perfil.getValue().getId());
        assertThat(resultado.getEnderecoId()).isEqualTo(endereco.getValue().getId());
    }

    private RegistrarCidadaoDTO novoCidadao() {
        return RegistrarCidadaoDTO.builder()
                .cpf(CPF)
                .nomeCompleto("Maria da Silva")
                .email("maria@example.com")
                .logradouro("Rua Principal")
                .numero("10")
                .bairro("Centro")
                .build();
    }

    @Test
    void trocaASenhaQuandoASenhaAtualConfere() {
        Usuario usuario = Usuario.builder().cpf(CPF).senhaHash("hash-antigo").build();
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordHasher.matches("senha-antiga", "hash-antigo")).thenReturn(true);
        when(passwordHasher.hash("senha-nova")).thenReturn("hash-novo");

        contaService.trocarSenha(usuario.getId(), "senha-antiga", "senha-nova");

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void recusaTrocaDeSenhaQuandoASenhaAtualEstaIncorreta() {
        Usuario usuario = Usuario.builder().cpf(CPF).senhaHash("hash-antigo").build();
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordHasher.matches("errada", "hash-antigo")).thenReturn(false);

        assertThatThrownBy(() -> contaService.trocarSenha(usuario.getId(), "errada", "senha-nova"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta.");

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-antigo");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void recusaTrocaDeSenhaDeUsuarioInexistente() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.trocarSenha(usuarioId, "qualquer", "senha-nova"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

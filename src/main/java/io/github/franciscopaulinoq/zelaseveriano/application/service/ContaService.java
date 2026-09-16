package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EnderecoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public String autenticar(String cpf, String senha) {
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("CPF ou senha inválidos"));

        if (!passwordHasher.matches(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("CPF ou senha inválidos");
        }

        return tokenService.gerarToken(usuario);
    }

    @Transactional
    public RegistrarCidadaoDTO registrarCidadao(RegistrarCidadaoDTO dto, String senhaPura) {
        if (usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new CidadaoJaRegistradoException("CPF já registrado no sistema");
        }

        String senhaHash = passwordHasher.hash(senhaPura);

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .cpf(dto.getCpf())
                .senhaHash(senhaHash)
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        dto.setUsuarioId(usuarioSalvo.getId());

        Perfil perfil = Perfil.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioSalvo)
                .nomeCompleto(dto.getNomeCompleto())
                .email(dto.getEmail())
                .build();

        Perfil perfilSalvo = perfilRepository.save(perfil);
        dto.setPerfilId(perfilSalvo.getId());

        Endereco endereco = Endereco.builder()
                .id(UUID.randomUUID())
                .perfil(perfilSalvo)
                .cep("59910000")
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade("Doutor Severiano")
                .uf("RN")
                .build();

        Endereco enderecoSalvo = enderecoRepository.save(endereco);
        dto.setEnderecoId(enderecoSalvo.getId());

        return dto;
    }
}

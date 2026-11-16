package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CidadaoRegistradoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SessaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CredenciaisInvalidasException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TokenInvalidoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Endereco;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.EnderecoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.PasswordHasher;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenRenovacaoPayload;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContaService {

  private static final String HASH_USUARIO_INEXISTENTE =
      "$2a$12$yG48DtlJmfemBdYb7Byuv./c35yRg/MUcOqGrcxZtrEZZ85MZeSm6";

  private final UsuarioRepository usuarioRepository;
  private final PerfilRepository perfilRepository;
  private final EnderecoRepository enderecoRepository;
  private final PasswordHasher passwordHasher;
  private final TokenService tokenService;

  @Transactional(readOnly = true)
  public SessaoDTO autenticar(String cpf, String senha) {
    Usuario usuario = usuarioRepository.findByCpf(cpf).orElse(null);
    String senhaHash = usuario == null ? HASH_USUARIO_INEXISTENTE : usuario.getSenhaHash();
    if (!passwordHasher.matches(senha, senhaHash) || usuario == null) {
      throw new CredenciaisInvalidasException("CPF ou senha inválidos");
    }

    return new SessaoDTO(
        tokenService.gerarToken(usuario), tokenService.gerarTokenDeRenovacao(usuario));
  }

  @Transactional(readOnly = true)
  public String renovarSessao(String tokenRenovacao) {
    TokenRenovacaoPayload payload =
        tokenService
            .validarTokenDeRenovacao(tokenRenovacao)
            .orElseThrow(
                () -> new TokenInvalidoException("Token de renovação inválido ou expirado."));
    Usuario usuario =
        usuarioRepository
            .findById(payload.usuarioId())
            .orElseThrow(
                () -> new TokenInvalidoException("Token de renovação inválido ou expirado."));
    if (usuario.getVersaoTokens() != payload.versaoTokens()) {
      throw new TokenInvalidoException("Token de renovação inválido ou expirado.");
    }

    return tokenService.gerarToken(usuario);
  }

  @Transactional
  public void trocarSenha(UUID usuarioId, String senhaAtual, String novaSenha) {
    Usuario usuario =
        usuarioRepository
            .findByIdParaAtualizacao(usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

    if (senhaAtual == null
        || senhaAtual.isBlank()
        || !passwordHasher.matches(senhaAtual, usuario.getSenhaHash())) {
      throw new IllegalArgumentException("Senha atual incorreta.");
    }

    usuario.alterarSenha(passwordHasher.hash(novaSenha));
    usuario.invalidarTokensDeRenovacao();
    usuarioRepository.save(usuario);
  }

  @Transactional
  public CidadaoRegistradoDTO registrarCidadao(RegistrarCidadaoDTO dto, String senhaPura) {
    if (usuarioRepository.existsByCpf(dto.getCpf())) {
      throw new CidadaoJaRegistradoException("CPF já registrado no sistema");
    }

    String senhaHash = passwordHasher.hash(senhaPura);
    Usuario usuario =
        Usuario.builder().id(UUID.randomUUID()).cpf(dto.getCpf()).senhaHash(senhaHash).build();

    Usuario usuarioSalvo;
    try {
      usuarioSalvo = usuarioRepository.save(usuario);
    } catch (DataIntegrityViolationException exception) {
      throw new CidadaoJaRegistradoException("CPF já registrado no sistema", exception);
    }

    Perfil perfil =
        Perfil.builder()
            .id(UUID.randomUUID())
            .usuario(usuarioSalvo)
            .nomeCompleto(dto.getNomeCompleto())
            .email(dto.getEmail())
            .build();
    Perfil perfilSalvo = perfilRepository.save(perfil);

    Endereco endereco =
        Endereco.builder()
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

    return new CidadaoRegistradoDTO(
        usuarioSalvo.getId(),
        perfilSalvo.getId(),
        enderecoSalvo.getId(),
        dto.getCpf(),
        dto.getNomeCompleto(),
        dto.getEmail(),
        "Cidadão registrado com sucesso");
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public PerfilMeDTO obterMeuPerfil(UUID usuarioId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado para o usuário logado."));

        return PerfilMeDTO.builder()
                .id(perfil.getId())
                .nomeCompleto(perfil.getNomeCompleto())
                .email(perfil.getEmail())
                .cpf(perfil.getUsuario().getCpf())
                .build();
    }
}

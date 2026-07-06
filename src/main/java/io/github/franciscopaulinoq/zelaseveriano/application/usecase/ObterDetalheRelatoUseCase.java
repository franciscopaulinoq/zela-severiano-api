package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObterDetalheRelatoUseCase {

    private final RelatoRepository relatoRepository;
    private final PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public RelatoDetalheDTO execute(UUID usuarioId, Long relatoId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil do usuário não encontrado."));

        Relato relato = relatoRepository.findByPerfilIdAndId(perfil.getId(), relatoId)
                .orElseThrow(() -> new IllegalArgumentException("Relato não encontrado ou não pertence a este usuário."));

        return RelatoDetalheDTO.builder()
                .id(relato.getId())
                .urlFoto(relato.getUrlFoto())
                .status(relato.getStatus())
                .criadoEm(relato.getCriadoEm())
                .categoriaNome(relato.getCategoria().getNome())
                .latitude(relato.getLatitude())
                .longitude(relato.getLongitude())
                .descricao(relato.getDescricao())
                .build();
    }
}
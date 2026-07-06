package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisualizarMeusRelatosUseCase {

    private final RelatoRepository relatoRepository;
    private final PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public List<RelatoListagemDTO> execute(UUID usuarioId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil do usuário não encontrado."));

        return relatoRepository.findAllByPerfilId(perfil.getId()).stream()
                .map(relato -> RelatoListagemDTO.builder()
                        .id(relato.getId())
                        .categoriaNome(relato.getCategoria().getNome())
                        .categoriaIconeNome(relato.getCategoria().getIconeNome())
                        .status(relato.getStatus())
                        .criadoEm(relato.getCriadoEm())
                        .build())
                .collect(Collectors.toList());
    }
}
package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatoService {

    private final RelatoRepository relatoRepository;
    private final PerfilRepository perfilRepository;
    private final CategoriaRepository categoriaRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Relato registrar(UUID usuarioId, RelatoRegistroDTO dto, byte[] fileBytes, String originalFilename, String contentType) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil do usuário não encontrado."));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        String urlFoto = null;
        if (fileBytes != null && fileBytes.length > 0) {
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            urlFoto = fileStorage.upload(fileBytes, fileName, contentType);
        }

        Relato relato = Relato.builder()
                .perfil(perfil)
                .categoria(categoria)
                .status(StatusRelato.PENDENTE)
                .descricao(dto.getDescricao())
                .urlFoto(urlFoto)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        return relatoRepository.save(relato);
    }

    @Transactional(readOnly = true)
    public List<RelatoListagemDTO> visualizarMeus(UUID usuarioId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil do usuário não encontrado."));

        return relatoRepository.findAllByPerfilId(perfil.getId()).stream()
                .map(this::toListagemDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatoDetalheDTO obterDetalhe(UUID usuarioId, Long relatoId) {
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil do usuário não encontrado."));

        Relato relato = relatoRepository.findByPerfilIdAndId(perfil.getId(), relatoId)
                .orElseThrow(() -> new IllegalArgumentException("Relato não encontrado ou não pertence a este usuário."));

        return toDetalheDTO(relato);
    }

    @Transactional(readOnly = true)
    public List<RelatoGestaoDTO> listarTodos() {
        return relatoRepository.findAll().stream()
                .map(this::toGestaoDTO)
                .toList();
    }

    @Transactional
    public Relato atualizarStatus(Long relatoId, StatusRelato novoStatus, String observacao) {
        Relato relato = relatoRepository.findById(relatoId)
                .orElseThrow(() -> new IllegalArgumentException("Relato não encontrado."));

        relato.atualizarStatus(novoStatus, observacao);

        return relatoRepository.save(relato);
    }

    private RelatoListagemDTO toListagemDTO(Relato relato) {
        return RelatoListagemDTO.builder()
                .id(relato.getId())
                .categoriaNome(relato.getCategoria().getNome())
                .categoriaIconeNome(relato.getCategoria().getIconeNome())
                .status(relato.getStatus())
                .criadoEm(relato.getCriadoEm())
                .build();
    }

    private RelatoDetalheDTO toDetalheDTO(Relato relato) {
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

    private RelatoGestaoDTO toGestaoDTO(Relato relato) {
        return RelatoGestaoDTO.builder()
                .id(relato.getId())
                .cidadaoNome(relato.getPerfil().getNomeCompleto())
                .categoriaNome(relato.getCategoria().getNome())
                .status(relato.getStatus())
                .descricao(relato.getDescricao())
                .observacaoResolucao(relato.getObservacaoResolucao())
                .criadoEm(relato.getCriadoEm())
                .atualizadoEm(relato.getAtualizadoEm())
                .build();
    }
}

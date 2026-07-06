package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarRelatoUseCase {

    private final RelatoRepository relatoRepository;
    private final PerfilRepository perfilRepository;
    private final CategoriaRepository categoriaRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Relato execute(UUID usuarioId, RelatoRegistroDTO dto, byte[] fileBytes, String originalFilename, String contentType) {
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
}
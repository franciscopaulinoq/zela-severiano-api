package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaEmUsoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final RelatoRepository relatoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public CategoriaDTO criar(CategoriaDTO dados) {
        Categoria categoria = Categoria.builder()
                .nome(dados.getNome())
                .iconeNome(dados.getIconeNome())
                .build();

        return toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO atualizar(Integer categoriaId, CategoriaDTO dados) {
        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        categoria.atualizar(dados.getNome(), dados.getIconeNome());

        return toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void remover(Integer categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }

        if (relatoRepository.existsByCategoriaIdAndStatus(categoriaId, StatusRelato.PENDENTE)) {
            throw new CategoriaEmUsoException("A categoria possui relatos vinculados e não pode ser removida.");
        }

        categoriaRepository.deleteById(categoriaId);
    }

    private CategoriaDTO toDTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .iconeNome(categoria.getIconeNome())
                .build();
    }
}

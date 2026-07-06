package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarCategoriasUseCase {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> execute() {
        return categoriaRepository.findAll().stream()
                .map(categoria -> CategoriaDTO.builder()
                        .id(categoria.getId())
                        .nome(categoria.getNome())
                        .iconeNome(categoria.getIconeNome())
                        .build())
                .collect(Collectors.toList());
    }
}
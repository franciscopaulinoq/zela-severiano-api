package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaEmUsoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private RelatoRepository relatoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void criaCategoriaComNomeEIcone() {
        when(categoriaRepository.save(any())).thenAnswer(invocacao -> {
            Categoria nova = invocacao.getArgument(0);
            return Categoria.builder()
                    .id(10)
                    .nome(nova.getNome())
                    .iconeNome(nova.getIconeNome())
                    .build();
        });

        CategoriaDTO criada = categoriaService.criar(new CategoriaDTO(null, "Calçadas", "person-walking"));

        assertThat(criada.getId()).isEqualTo(10);
        assertThat(criada.getNome()).isEqualTo("Calçadas");
        assertThat(criada.getIconeNome()).isEqualTo("person-walking");
    }

    @Test
    void atualizaNomeEIconeDeCategoriaExistente() {
        Categoria existente =
                Categoria.builder().id(3).nome("Limpeza").iconeNome("trash").build();
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        CategoriaDTO atualizada = categoriaService.atualizar(3, new CategoriaDTO(null, "Limpeza urbana", "broom"));

        assertThat(atualizada.getNome()).isEqualTo("Limpeza urbana");
        assertThat(atualizada.getIconeNome()).isEqualTo("broom");
    }

    @Test
    void recusaAtualizarCategoriaInexistente() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.atualizar(99, new CategoriaDTO(null, "X", "y")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeCategoriaSemRelatos() {
        when(categoriaRepository.existsById(7)).thenReturn(true);
        when(relatoRepository.existsByCategoriaIdAndStatus(7, StatusRelato.PENDENTE))
                .thenReturn(false);

        categoriaService.remover(7);

        verify(categoriaRepository).deleteById(7);
    }

    @Test
    void recusaRemoverCategoriaComRelatoVinculado() {
        when(categoriaRepository.existsById(7)).thenReturn(true);
        when(relatoRepository.existsByCategoriaIdAndStatus(7, StatusRelato.PENDENTE))
                .thenReturn(true);

        assertThatThrownBy(() -> categoriaService.remover(7)).isInstanceOf(CategoriaEmUsoException.class);

        verify(categoriaRepository, never()).deleteById(any());
    }
}

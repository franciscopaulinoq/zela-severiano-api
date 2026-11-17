package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.DisponibilidadeFotoDTO.SituacaoFoto;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RelatoDisponibilidadeFotoServiceTest {
  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private CategoriaRepository categoriaRepository;
  @Mock private FileStorage fileStorage;
  @Mock private HistoricoStatusRelatoRepository historicoRepository;
  @InjectMocks private RelatoService service;

  @Test
  void distingueFotosDisponiveisAusentesEInexistentes() {
    var pagina = PageRequest.of(0, 20);
    var filtro = new FiltroRelatos(StatusRelato.PENDENTE, 1);
    when(relatoRepository.findAll(filtro, pagina))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    relato(1L, "https://fotos.test/presente.jpg"),
                    relato(2L, null),
                    relato(3L, "https://fotos.test/ausente.jpg")),
                pagina,
                3));
    when(fileStorage.existe("https://fotos.test/presente.jpg")).thenReturn(true);
    when(fileStorage.existe("https://fotos.test/ausente.jpg")).thenReturn(false);

    var resultado = service.verificarDisponibilidadeFotos(filtro, pagina);

    assertThat(resultado.getContent())
        .extracting("relatoId", "situacao")
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple(1L, SituacaoFoto.DISPONIVEL),
            org.assertj.core.groups.Tuple.tuple(2L, SituacaoFoto.SEM_FOTO),
            org.assertj.core.groups.Tuple.tuple(3L, SituacaoFoto.INDISPONIVEL));
    assertThat(resultado.getTotalElements()).isEqualTo(3);
    verify(fileStorage).existe("https://fotos.test/presente.jpg");
    verify(fileStorage).existe("https://fotos.test/ausente.jpg");
    verify(fileStorage, never()).existe(null);
  }

  @Test
  void paginaVaziaNaoAcessaArmazenamento() {
    var pagina = PageRequest.of(2, 10);
    var filtro = FiltroRelatos.semFiltro();
    when(relatoRepository.findAll(filtro, pagina))
        .thenReturn(new PageImpl<>(List.of(), pagina, 20));

    assertThat(service.verificarDisponibilidadeFotos(filtro, pagina)).isEmpty();
    verify(relatoRepository).findAll(filtro, pagina);
  }

  private static Relato relato(Long id, String urlFoto) {
    return Relato.builder().id(id).urlFoto(urlFoto).build();
  }
}

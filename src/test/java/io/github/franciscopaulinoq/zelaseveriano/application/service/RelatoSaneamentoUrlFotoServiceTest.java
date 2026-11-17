package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.UrlPublicaArquivo;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RelatoSaneamentoUrlFotoServiceTest {
  private static final String BASE = "https://cdn.test/base/";
  private static final String PREFIXO = "https://cdn.test/base//";
  private static final PageRequest LOTE = PageRequest.of(0, 50);

  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private CategoriaRepository categoriaRepository;
  @Mock private FileStorage fileStorage;
  @Mock private HistoricoStatusRelatoRepository historicoRepository;
  @InjectMocks private RelatoService service;

  @Test
  void processaLotesReiniciandoConsultaNaPrimeiraPagina() {
    List<Relato> relatos =
        LongStream.rangeClosed(1, 50)
            .mapToObj(id -> Relato.builder().id(id).urlFoto(PREFIXO + id + ".jpg").build())
            .toList();
    when(relatoRepository.findByUrlFotoStartingWith(PREFIXO, LOTE))
        .thenReturn(new PageImpl<>(relatos, LOTE, 50))
        .thenReturn(new PageImpl<>(List.of(), LOTE, 0));
    when(relatoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    int atualizados = service.sanearUrlsFotos(BASE);

    assertThat(atualizados).isEqualTo(50);
    verify(relatoRepository, times(2)).findByUrlFotoStartingWith(PREFIXO, LOTE);
    verify(relatoRepository, times(50)).save(any());
    assertThat(relatos)
        .allSatisfy(relato ->
            assertThat(relato.getUrlFoto())
                .startsWith("https://cdn.test/base/")
                .doesNotContain("base//"));
  }

  @Test
  void nenhumCandidatoNaoExecutaAtualizacao() {
    when(relatoRepository.findByUrlFotoStartingWith(PREFIXO, LOTE))
        .thenReturn(new PageImpl<>(List.of(), LOTE, 0));

    assertThat(service.sanearUrlsFotos(BASE)).isZero();

    verify(relatoRepository, never()).save(any());
  }

  @Test
  void baseSemBarraProduzMesmoPrefixoDeCandidatos() {
    when(relatoRepository.findByUrlFotoStartingWith(PREFIXO, LOTE))
        .thenReturn(new PageImpl<>(List.of(), LOTE, 0));

    service.sanearUrlsFotos("https://cdn.test/base");

    verify(relatoRepository)
        .findByUrlFotoStartingWith(UrlPublicaArquivo.prefixoCandidatos(BASE), LOTE);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.ComentarioRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.DetalheGestaoRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.RelatoFoto;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.ComentarioRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoDetalheRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoFotoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatoGaleriaServiceTest {
  private static final UUID GESTOR_ID = UUID.randomUUID();
  private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private CategoriaRepository categoriaRepository;
  @Mock private FileStorage fileStorage;
  @Mock private HistoricoStatusRelatoRepository historicoRepository;
  @Mock private RelatoDetalheRepository detalheRepository;
  @Mock private RelatoFotoRepository fotoRepository;
  @Mock private ComentarioRelatoRepository comentarioRepository;
  @InjectMocks private RelatoService service;

  @Test
  void detalheAmpliadoMantemFotosEComentariosDasColecoes() {
    Relato relato = Relato.builder().id(7L).urlFoto("original.jpg").build();
    RelatoFoto foto = RelatoFoto.builder().id(1L).urlFoto("complementar.jpg").build();
    ComentarioRelato comentario = ComentarioRelato.builder().id(2L).texto("Em análise").build();
    when(detalheRepository.buscarDetalheCompleto(7L))
        .thenReturn(
            Optional.of(new DetalheGestaoRelato(relato, List.of(foto), List.of(comentario))));

    var resultado = service.obterDetalheGestao(7L);

    assertThat(resultado.urlFoto()).isEqualTo("original.jpg");
    assertThat(resultado.fotos()).extracting("urlFoto").containsExactly("complementar.jpg");
    assertThat(resultado.comentarios()).extracting("texto").containsExactly("Em análise");
  }

  @Test
  void detalheAmpliadoInexistenteRetornaNaoEncontrado() {
    when(detalheRepository.buscarDetalheCompleto(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obterDetalheGestao(99L))
        .isInstanceOf(RecursoNaoEncontradoException.class);
  }

  @Test
  void adicionaFotoSemSubstituirOriginal() {
    when(relatoRepository.findByIdParaAtualizacao(7L))
        .thenReturn(Optional.of(Relato.builder().id(7L).urlFoto("original.jpg").build()));
    when(fotoRepository.countByRelatoId(7L)).thenReturn(0L);
    when(fileStorage.upload(eq(JPEG), anyString(), eq("image/jpeg"))).thenReturn("nova.jpg");
    when(fotoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var resultado = service.adicionarFotoGestao(GESTOR_ID, 7L, JPEG, "foto.jpeg", "image/jpeg");

    assertThat(resultado.urlFoto()).isEqualTo("nova.jpg");
    assertThat(resultado.autorId()).isEqualTo(GESTOR_ID);
    assertThat(resultado.criadoEm()).isNotNull();
  }

  @Test
  void limiteDeFotosEvitaNovoUpload() {
    when(relatoRepository.findByIdParaAtualizacao(7L))
        .thenReturn(Optional.of(Relato.builder().id(7L).build()));
    when(fotoRepository.countByRelatoId(7L)).thenReturn(10L);

    assertThatThrownBy(
            () -> service.adicionarFotoGestao(GESTOR_ID, 7L, JPEG, "foto.jpg", "image/jpeg"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dez fotos");
    verify(fileStorage, never()).upload(any(), anyString(), anyString());
  }

  @Test
  void comentarioRegistraAutorDataETextoSemEspacosExternos() {
    when(relatoRepository.findById(7L)).thenReturn(Optional.of(Relato.builder().id(7L).build()));
    when(comentarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var resultado = service.adicionarComentarioGestao(GESTOR_ID, 7L, "  Equipe acionada  ");

    ArgumentCaptor<ComentarioRelato> captor = ArgumentCaptor.forClass(ComentarioRelato.class);
    verify(comentarioRepository).save(captor.capture());
    assertThat(captor.getValue().getTexto()).isEqualTo("Equipe acionada");
    assertThat(captor.getValue().getCriadoEm()).isInstanceOf(OffsetDateTime.class);
    assertThat(resultado.autorId()).isEqualTo(GESTOR_ID);
  }
}

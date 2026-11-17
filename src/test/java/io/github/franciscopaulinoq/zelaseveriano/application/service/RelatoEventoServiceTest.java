package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RelatoEventoServiceTest {
  private static final Perfil PERFIL =
      Perfil.builder().id(UUID.randomUUID()).email("autor@example.com").build();
  private static final Categoria CATEGORIA = Categoria.builder().id(1).nome("Iluminação").build();

  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private CategoriaRepository categoriaRepository;
  @Mock private FileStorage fileStorage;
  @Mock private HistoricoStatusRelatoRepository historicoRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private RelatoService relatoService;

  @Test
  void eventoEhPublicadoDepoisDoSaveEDoHistorico() {
    Relato relato = relatoPendente();
    when(relatoRepository.findByIdParaAtualizacao(42L)).thenReturn(Optional.of(relato));
    when(relatoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

    relatoService.atualizarStatus(42L, StatusRelato.EM_ANALISE, null);

    var salvamento = mockingDetails(relatoRepository).getInvocations().iterator().next();
    var historico = mockingDetails(historicoRepository).getInvocations().iterator().next();
    var publicacao = mockingDetails(eventPublisher).getInvocations().iterator().next();
    assertThat(historico.getSequenceNumber()).isGreaterThan(salvamento.getSequenceNumber());
    assertThat(publicacao.getSequenceNumber()).isGreaterThan(historico.getSequenceNumber());
  }

  @Test
  void falhaAoSalvarRelatoNaoPublicaEvento() {
    Relato relato = relatoPendente();
    when(relatoRepository.findByIdParaAtualizacao(42L)).thenReturn(Optional.of(relato));
    when(relatoRepository.save(any())).thenThrow(new IllegalStateException("Falha ao salvar"));

    assertThatThrownBy(() -> relatoService.atualizarStatus(42L, StatusRelato.EM_ANALISE, null))
        .isInstanceOf(IllegalStateException.class);
    verifyNoInteractions(historicoRepository, eventPublisher);
  }

  @Test
  void falhaAoGravarHistoricoNaoPublicaEvento() {
    Relato relato = relatoPendente();
    when(relatoRepository.findByIdParaAtualizacao(42L)).thenReturn(Optional.of(relato));
    when(relatoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    doThrow(new IllegalStateException("Falha no histórico")).when(historicoRepository).save(any());

    assertThatThrownBy(() -> relatoService.atualizarStatus(42L, StatusRelato.EM_ANALISE, null))
        .isInstanceOf(IllegalStateException.class);
    verifyNoInteractions(eventPublisher);
  }

  private Relato relatoPendente() {
    return Relato.builder()
        .id(42L)
        .perfil(PERFIL)
        .categoria(CATEGORIA)
        .status(StatusRelato.PENDENTE)
        .build();
  }
}

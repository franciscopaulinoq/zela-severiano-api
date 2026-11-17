package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AgendarVistoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.ConflitoVistoriaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Vistoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.VistoriaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VistoriaServiceTest {
  private static final UUID GESTOR_ID = UUID.randomUUID();
  private static final UUID USUARIO_ID = UUID.randomUUID();
  private static final Perfil PERFIL = Perfil.builder().id(UUID.randomUUID()).build();
  private static final OffsetDateTime INICIO = OffsetDateTime.parse("2026-06-10T10:00:00-03:00");
  private static final OffsetDateTime FIM = INICIO.plusHours(1);
  private static final AgendarVistoriaDTO DADOS = new AgendarVistoriaDTO(INICIO, FIM);

  @Mock private VistoriaRepository vistoriaRepository;
  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @InjectMocks private VistoriaService vistoriaService;

  @Test
  void agendaVistoriaEmIntervaloLivre() {
    when(relatoRepository.existsById(42L)).thenReturn(true);
    when(vistoriaRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

    var resultado = vistoriaService.agendar(GESTOR_ID, 42L, DADOS);

    assertThat(resultado.relatoId()).isEqualTo(42L);
    assertThat(resultado.gestorId()).isEqualTo(GESTOR_ID);
    assertThat(resultado.status()).isEqualTo(StatusVistoria.ATIVA);
    verify(vistoriaRepository).existeConflito(GESTOR_ID, INICIO, FIM);
  }

  @Test
  void agendamentoComConflitoNaoSalva() {
    when(relatoRepository.existsById(42L)).thenReturn(true);
    when(vistoriaRepository.existeConflito(GESTOR_ID, INICIO, FIM)).thenReturn(true);

    assertThatThrownBy(() -> vistoriaService.agendar(GESTOR_ID, 42L, DADOS))
        .isInstanceOf(ConflitoVistoriaException.class);
    verify(vistoriaRepository, never()).save(any());
  }

  @Test
  void agendamentoInvalidoNaoConsultaBanco() {
    AgendarVistoriaDTO invalido = new AgendarVistoriaDTO(FIM, INICIO);

    assertThatThrownBy(() -> vistoriaService.agendar(GESTOR_ID, 42L, invalido))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(relatoRepository, vistoriaRepository, perfilRepository);
  }

  @Test
  void agendamentoExigeRelatoExistente() {
    when(relatoRepository.existsById(42L)).thenReturn(false);

    assertThatThrownBy(() -> vistoriaService.agendar(GESTOR_ID, 42L, DADOS))
        .isInstanceOf(IllegalArgumentException.class);
    verify(vistoriaRepository, never()).save(any());
  }

  @Test
  void cancelamentoMarcaVistoriaComoCancelada() {
    Vistoria vistoria = Vistoria.agendar(42L, GESTOR_ID, INICIO, FIM);
    when(vistoriaRepository.findById(7L)).thenReturn(Optional.of(vistoria));
    when(vistoriaRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

    vistoriaService.cancelar(7L);

    assertThat(vistoria.getStatus()).isEqualTo(StatusVistoria.CANCELADA);
    verify(vistoriaRepository).save(vistoria);
  }

  @Test
  void cidadaoListaVistoriasDoProprioRelato() {
    Vistoria vistoria = Vistoria.agendar(42L, GESTOR_ID, INICIO, FIM);
    when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
    when(relatoRepository.existsByPerfilIdAndId(PERFIL.getId(), 42L)).thenReturn(true);
    when(vistoriaRepository.findAllByRelatoIdOrderByInicio(42L)).thenReturn(List.of(vistoria));

    var resultado = vistoriaService.listarDoCidadao(USUARIO_ID, 42L);

    assertThat(resultado).singleElement().extracting(item -> item.relatoId()).isEqualTo(42L);
  }

  @Test
  void cidadaoNaoListaVistoriasDeOutroRelato() {
    when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
    when(relatoRepository.existsByPerfilIdAndId(PERFIL.getId(), 42L)).thenReturn(false);

    assertThatThrownBy(() -> vistoriaService.listarDoCidadao(USUARIO_ID, 42L))
        .isInstanceOf(IllegalArgumentException.class);
    verify(vistoriaRepository, never()).findAllByRelatoIdOrderByInicio(any());
  }

  @Test
  void listagemDaGestaoExigeRelatoExistente() {
    when(relatoRepository.existsById(42L)).thenReturn(false);

    assertThatThrownBy(() -> vistoriaService.listarGestao(42L))
        .isInstanceOf(IllegalArgumentException.class);
    verify(vistoriaRepository, never()).findAllByRelatoIdOrderByInicio(any());
  }
}

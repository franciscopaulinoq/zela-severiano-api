package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class TransicaoStatusConcorrenciaIntegrationTest {

  @Autowired private RelatoService relatoService;

  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void apenasUmaTransicaoConcorrenteEhAceita() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Gestor Concorrente");
    Long relatoId =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Poste apagado", -6.08, -38.38),
                null,
                null,
                null)
            .getId();
    CountDownLatch prontas = new CountDownLatch(2);
    CountDownLatch iniciar = new CountDownLatch(1);

    int transicoesConcluidas = 0;
    int transicoesRecusadas = 0;
    try (var executor = Executors.newFixedThreadPool(2)) {
      List<Callable<StatusRelato>> chamadas =
          List.of(
              transicao(relatoId, StatusRelato.EM_ANALISE, prontas, iniciar),
              cancelamento(relatoId, cidadao, prontas, iniciar));
      List<Future<StatusRelato>> resultados = chamadas.stream().map(executor::submit).toList();
      prontas.await();
      iniciar.countDown();
      for (Future<StatusRelato> resultado : resultados) {
        try {
          resultado.get();
          transicoesConcluidas++;
        } catch (ExecutionException exception) {
          assertThat(exception.getCause()).isInstanceOf(TransicaoStatusInvalidaException.class);
          transicoesRecusadas++;
        }
      }
    }

    assertThat(transicoesConcluidas).isEqualTo(1);
    assertThat(transicoesRecusadas).isEqualTo(1);
    assertThat(relatoService.obterHistoricoGestao(relatoId)).hasSize(1);
    assertThat(relatoService.obterDetalhe(cidadao.usuarioId(), relatoId).getStatus())
        .isIn(StatusRelato.EM_ANALISE, StatusRelato.CANCELADO);
  }

  private Callable<StatusRelato> transicao(
      Long relatoId, StatusRelato status, CountDownLatch prontas, CountDownLatch iniciar) {
    return () -> {
      prontas.countDown();
      iniciar.await();
      relatoService.atualizarStatus(relatoId, status, "Avaliação");
      return status;
    };
  }

  private Callable<StatusRelato> cancelamento(
      Long relatoId, Cidadao cidadao, CountDownLatch prontas, CountDownLatch iniciar) {
    return () -> {
      prontas.countDown();
      iniciar.await();
      relatoService.cancelar(cidadao.usuarioId(), relatoId);
      return StatusRelato.CANCELADO;
    };
  }
}

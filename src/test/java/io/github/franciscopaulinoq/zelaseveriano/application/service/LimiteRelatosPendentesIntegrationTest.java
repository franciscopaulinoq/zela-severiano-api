package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoCriadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.LimiteRelatosPendentesException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class LimiteRelatosPendentesIntegrationTest {

  @Autowired private RelatoService relatoService;

  @Autowired private RelatoRepository relatoRepository;

  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void sextoRelatoPendenteEhRecusado() {
    Cidadao cidadao = cidadaos.cadastrar("Pedro Alves");
    for (int i = 0; i < RelatoService.LIMITE_RELATOS_PENDENTES; i++) {
      registrar(cidadao);
    }

    assertThatThrownBy(() -> registrar(cidadao))
        .isInstanceOf(LimiteRelatosPendentesException.class);
    assertThat(relatoRepository.countByPerfilIdAndStatus(cidadao.perfilId(), StatusRelato.PENDENTE))
        .isEqualTo(RelatoService.LIMITE_RELATOS_PENDENTES);
  }

  @Test
  void quandoUmPendenteEntraEmAnaliseOCidadaoVoltaAPoderRegistrar() {
    Cidadao cidadao = cidadaos.cadastrar("Paulo Alves");
    List<RelatoCriadoDTO> pendentes = new ArrayList<>();
    for (int i = 0; i < RelatoService.LIMITE_RELATOS_PENDENTES; i++) {
      pendentes.add(registrar(cidadao));
    }

    relatoService.atualizarStatus(pendentes.getFirst().getId(), StatusRelato.EM_ANALISE, null);
    RelatoCriadoDTO novo = registrar(cidadao);

    assertThat(novo.getStatus()).isEqualTo(StatusRelato.PENDENTE);
    assertThat(relatoRepository.countByPerfilIdAndStatus(cidadao.perfilId(), StatusRelato.PENDENTE))
        .isEqualTo(RelatoService.LIMITE_RELATOS_PENDENTES);
  }

  @Test
  void registrosSimultaneosNaoExcedemOLimite() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Concorrente da Silva");
    List<Callable<RelatoCriadoDTO>> chamadas = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      chamadas.add(() -> registrar(cidadao));
    }

    int registrosConcluidos = 0;
    try (var executor = Executors.newFixedThreadPool(8)) {
      List<Future<RelatoCriadoDTO>> resultados = executor.invokeAll(chamadas);
      for (Future<RelatoCriadoDTO> resultado : resultados) {
        try {
          resultado.get();
          registrosConcluidos++;
        } catch (ExecutionException exception) {
          assertThat(exception.getCause()).isInstanceOf(LimiteRelatosPendentesException.class);
        }
      }
    }

    assertThat(registrosConcluidos).isEqualTo(RelatoService.LIMITE_RELATOS_PENDENTES);
    assertThat(relatoRepository.countByPerfilIdAndStatus(cidadao.perfilId(), StatusRelato.PENDENTE))
        .isEqualTo(RelatoService.LIMITE_RELATOS_PENDENTES);
  }

  private RelatoCriadoDTO registrar(Cidadao cidadao) {
    return relatoService.registrar(
        cidadao.usuarioId(),
        new RelatoRegistroDTO(4, "Buraco na rua", -6.08, -38.38),
        null,
        null,
        null);
  }
}

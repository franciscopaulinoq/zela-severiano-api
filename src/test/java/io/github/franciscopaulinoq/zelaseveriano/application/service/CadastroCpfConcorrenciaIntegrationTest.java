package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CidadaoJaRegistradoException;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class CadastroCpfConcorrenciaIntegrationTest {

  private static final String CPF = "52998224725";

  @Autowired private ContaService contaService;

  @Test
  void apenasUmCadastroComOMesmoCpfEhConcluido() throws Exception {
    CountDownLatch prontos = new CountDownLatch(2);
    CountDownLatch iniciar = new CountDownLatch(1);
    int cadastrosConcluidos = 0;
    int cadastrosRecusados = 0;

    try (var executor = Executors.newFixedThreadPool(2)) {
      List<Callable<Void>> chamadas =
          List.of(cadastro(prontos, iniciar), cadastro(prontos, iniciar));
      List<Future<Void>> resultados = chamadas.stream().map(executor::submit).toList();
      prontos.await();
      iniciar.countDown();
      for (Future<Void> resultado : resultados) {
        try {
          resultado.get();
          cadastrosConcluidos++;
        } catch (ExecutionException exception) {
          assertThat(exception.getCause()).isInstanceOf(CidadaoJaRegistradoException.class);
          cadastrosRecusados++;
        }
      }
    }

    assertThat(cadastrosConcluidos).isEqualTo(1);
    assertThat(cadastrosRecusados).isEqualTo(1);
  }

  private Callable<Void> cadastro(CountDownLatch prontos, CountDownLatch iniciar) {
    return () -> {
      RegistrarCidadaoDTO dto =
          RegistrarCidadaoDTO.builder()
              .cpf(CPF)
              .nomeCompleto("Cidadão Concorrente")
              .email("concorrente@example.com")
              .logradouro("Rua Principal")
              .numero("10")
              .bairro("Centro")
              .build();
      prontos.countDown();
      iniciar.await();
      contaService.registrarCidadao(dto, "senha-secreta");
      return null;
    };
  }
}

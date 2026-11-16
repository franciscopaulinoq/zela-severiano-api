package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SaneamentoUrlFotoConfigTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(SaneamentoUrlFotoConfig.class);

  @Test
  void runnerFicaDesativadoPorPadrao() {
    contextRunner
        .withBean(RelatoService.class, () -> mock(RelatoService.class))
        .run(context -> assertThat(context).doesNotHaveBean("saneamentoUrlFotoRunner"));
  }

  @Test
  void runnerPodeSerAtivadoPelaPropriedade() {
    RelatoService relatoService = mock(RelatoService.class);

    contextRunner
        .withBean(RelatoService.class, () -> relatoService)
        .withPropertyValues(
            "api.saneamento.urls-fotos.enabled=true",
            "cloudflare.r2.public-url=http://localhost:9000/test")
        .run(context -> {
          assertThat(context).hasBean("saneamentoUrlFotoRunner");
          context.getBean("saneamentoUrlFotoRunner", ApplicationRunner.class).run(null);
          verify(relatoService).sanearUrlsFotos("http://localhost:9000/test");
        });
  }

  @Test
  void runnerUsaEnderecoPublicoConfigurado() throws Exception {
    RelatoService relatoService = mock(RelatoService.class);

    new SaneamentoUrlFotoConfig(relatoService)
        .saneamentoUrlFotoRunner("https://cdn.test/base/")
        .run(null);

    verify(relatoService).sanearUrlsFotos("https://cdn.test/base/");
  }
}

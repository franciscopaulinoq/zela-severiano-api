package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaneamentoUrlFotoConfig {
  private final RelatoService relatoService;

  @Bean
  @ConditionalOnProperty(name = "api.saneamento.urls-fotos.enabled", havingValue = "true")
  ApplicationRunner saneamentoUrlFotoRunner(
      @Value("${cloudflare.r2.public-url}") String publicUrl) {
    return args -> {
      log.info("Iniciando saneamento de URLs de fotos");
      int atualizados = relatoService.sanearUrlsFotos(publicUrl);
      log.info("Saneamento de URLs de fotos concluído: {} registros atualizados", atualizados);
    };
  }
}

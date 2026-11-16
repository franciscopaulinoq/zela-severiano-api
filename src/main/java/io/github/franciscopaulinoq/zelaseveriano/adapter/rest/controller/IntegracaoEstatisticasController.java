package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.IntegracaoEstatisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integracoes/estatisticas")
@RequiredArgsConstructor
@SecurityScheme(
    name = "apiKey",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-API-Key")
public class IntegracaoEstatisticasController {

  private final IntegracaoEstatisticasService service;

  @Operation(summary = "Obtém estatísticas agregadas para integrações parceiras")
  @SecurityRequirement(name = "apiKey")
  @GetMapping
  public ResponseEntity<EstatisticasRelatosDTO> obterEstatisticas() {
    return ResponseEntity.ok(service.obterEstatisticas());
  }
}

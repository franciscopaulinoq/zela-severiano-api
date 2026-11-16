package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AuditoriaGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.AuditoriaGestaoService;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/auditoria")
@RequiredArgsConstructor
public class GestaoAuditoriaController {
  private final AuditoriaGestaoService auditoriaService;

  @GetMapping
  public ResponseEntity<List<AuditoriaGestaoDTO>> consultar(
      @Parameter(description = "Filtra pelo identificador do gestor")
          @RequestParam(required = false)
          UUID gestorId,
      @Parameter(description = "Filtra pelo método HTTP") @RequestParam(required = false)
          String metodo) {
    return ResponseEntity.ok(auditoriaService.consultar(gestorId, metodo));
  }
}

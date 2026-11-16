package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoPublicoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/relatos")
@RequiredArgsConstructor
public class RelatoPublicoController {

  private final RelatoService relatoService;

  @GetMapping
  public ResponseEntity<Page<RelatoPublicoDTO>> listar(
      @Parameter(description = "Filtra pela categoria do relato") @RequestParam(required = false)
          Integer categoriaId,
      @Parameter(hidden = true) Pageable pageable) {
    return ResponseEntity.ok(relatoService.listarPublicos(categoriaId, pageable));
  }
}

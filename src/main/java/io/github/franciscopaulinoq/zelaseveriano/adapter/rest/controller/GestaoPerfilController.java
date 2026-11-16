package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.PerfilService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/perfis")
@RequiredArgsConstructor
public class GestaoPerfilController {

  private final PerfilService perfilService;

  @GetMapping("/{id}/contato")
  public ResponseEntity<PerfilMeDTO> obterContato(@PathVariable("id") UUID perfilId) {
    return ResponseEntity.ok(perfilService.obterContatoCidadao(perfilId));
  }
}

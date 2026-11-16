package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConfirmarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RecuperacaoSenhaSolicitadaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SolicitarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/recuperacao")
@RequiredArgsConstructor
public class RecuperacaoSenhaController {

  private final RecuperacaoSenhaService recuperacaoService;

  @PostMapping("/solicitar")
  public ResponseEntity<RecuperacaoSenhaSolicitadaDTO> solicitar(
      @Valid @RequestBody SolicitarRecuperacaoSenhaDTO dados) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(recuperacaoService.solicitar(dados));
  }

  @PostMapping("/confirmar")
  public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarRecuperacaoSenhaDTO dados) {
    recuperacaoService.confirmar(dados);
    return ResponseEntity.noContent().build();
  }
}

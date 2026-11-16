package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CidadaoRegistradoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cidadaos")
@RequiredArgsConstructor
public class CidadaoController {

  private final ContaService contaService;

  @PostMapping
  public ResponseEntity<CidadaoRegistradoDTO> registrar(
      @Valid @RequestBody RegistrarCidadaoDTO dados) {
    CidadaoRegistradoDTO resultado = contaService.registrarCidadao(dados, dados.getSenha());
    return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
  }
}

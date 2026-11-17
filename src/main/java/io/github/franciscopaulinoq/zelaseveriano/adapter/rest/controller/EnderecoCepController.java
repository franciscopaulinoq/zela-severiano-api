package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConsultaCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EnderecoCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ConsultaCepService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enderecos/cep")
@RequiredArgsConstructor
public class EnderecoCepController {
  private final ConsultaCepService consultaCepService;

  @PostMapping
  @Operation(summary = "Consulta dados de endereço por CEP sem alterar o perfil")
  public ResponseEntity<EnderecoCepDTO> consultar(@Valid @RequestBody ConsultaCepDTO dados) {
    return ResponseEntity.ok(consultaCepService.consultar(dados));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.LoginDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RenovarSessaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SessaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final ContaService contaService;

  @PostMapping("/login")
  public ResponseEntity<SessaoDTO> login(@Valid @RequestBody LoginDTO dados) {
    SessaoDTO sessao = contaService.autenticar(dados.getCpf(), dados.getSenha());
    return ResponseEntity.ok(sessao);
  }

  @PostMapping("/refresh")
  public ResponseEntity<SessaoDTO> renovarSessao(@Valid @RequestBody RenovarSessaoDTO dados) {
    String token = contaService.renovarSessao(dados.getRefreshToken());
    return ResponseEntity.ok(new SessaoDTO(token, null));
  }
}

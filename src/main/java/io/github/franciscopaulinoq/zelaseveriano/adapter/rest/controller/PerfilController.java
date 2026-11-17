package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarPerfilDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConfirmarEmailDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.PerfilMeDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.TrocarSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.PerfilService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfis")
@RequiredArgsConstructor
public class PerfilController {

  private final PerfilService perfilService;
  private final ContaService contaService;

  @GetMapping("/me")
  public ResponseEntity<PerfilMeDTO> obterMeuPerfil(
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID usuarioId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(perfilService.obterMeuPerfil(usuarioId));
  }

  @PatchMapping("/me")
  public ResponseEntity<PerfilMeDTO> atualizarMeuPerfil(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody AtualizarPerfilDTO dados) {
    UUID usuarioId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(perfilService.atualizarMeuPerfil(usuarioId, dados));
  }

  @PostMapping("/me/email/confirmacao")
  public ResponseEntity<PerfilMeDTO> confirmarEmail(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody ConfirmarEmailDTO dados) {
    UUID usuarioId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(perfilService.confirmarEmail(usuarioId, dados.codigo()));
  }

  @PatchMapping("/me/senha")
  public ResponseEntity<Void> trocarSenha(
      @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody TrocarSenhaDTO dados) {
    UUID usuarioId = UUID.fromString(userDetails.getUsername());
    contaService.trocarSenha(usuarioId, dados.getSenhaAtual(), dados.getNovaSenha());
    return ResponseEntity.noContent().build();
  }
}

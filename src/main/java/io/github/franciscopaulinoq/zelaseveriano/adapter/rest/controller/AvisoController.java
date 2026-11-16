package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoLeituraLoteDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoPublicacaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.AvisoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AvisoController {

  private final AvisoService avisoService;

  @PostMapping("/api/v1/gestao/avisos")
  public ResponseEntity<AvisoDTO> publicar(@Valid @RequestBody AvisoPublicacaoDTO dados) {
    return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.publicar(dados));
  }

  @GetMapping("/api/v1/avisos")
  public ResponseEntity<List<AvisoDTO>> listarMeus(@AuthenticationPrincipal UserDetails usuario) {
    return ResponseEntity.ok(avisoService.listarMeus(UUID.fromString(usuario.getUsername())));
  }

  @PatchMapping("/api/v1/avisos/leitura")
  public ResponseEntity<Void> marcarComoLidos(
      @AuthenticationPrincipal UserDetails usuario, @Valid @RequestBody AvisoLeituraLoteDTO dados) {
    avisoService.marcarComoLidos(UUID.fromString(usuario.getUsername()), dados.avisoIds());
    return ResponseEntity.noContent().build();
  }
}

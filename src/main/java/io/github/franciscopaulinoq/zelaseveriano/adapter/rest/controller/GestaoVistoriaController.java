package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AgendarVistoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.VistoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.VistoriaService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao")
@RequiredArgsConstructor
public class GestaoVistoriaController {
  private final VistoriaService vistoriaService;

  @PostMapping("/relatos/{relatoId}/vistorias")
  public ResponseEntity<VistoriaDTO> agendar(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long relatoId,
      @Valid @RequestBody AgendarVistoriaDTO dados) {
    UUID gestorId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(vistoriaService.agendar(gestorId, relatoId, dados));
  }

  @GetMapping("/relatos/{relatoId}/vistorias")
  public ResponseEntity<List<VistoriaDTO>> listar(@PathVariable Long relatoId) {
    return ResponseEntity.ok(vistoriaService.listarGestao(relatoId));
  }

  @PatchMapping("/vistorias/{vistoriaId}/cancelamento")
  public ResponseEntity<Void> cancelar(@PathVariable Long vistoriaId) {
    vistoriaService.cancelar(vistoriaId);
    return ResponseEntity.noContent().build();
  }
}

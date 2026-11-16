package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.WebhookDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.WebhookRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.WebhookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/webhooks")
@RequiredArgsConstructor
public class GestaoWebhookController {
  private final WebhookService webhookService;

  @PostMapping
  public ResponseEntity<WebhookDTO> cadastrar(@Valid @RequestBody WebhookRegistroDTO dados) {
    return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.cadastrar(dados.url()));
  }

  @GetMapping
  public ResponseEntity<List<WebhookDTO>> listar() {
    return ResponseEntity.ok(webhookService.listar());
  }

  @PatchMapping("/{id}/inativacao")
  public ResponseEntity<Void> inativar(@PathVariable UUID id) {
    webhookService.inativar(id);
    return ResponseEntity.noContent().build();
  }
}

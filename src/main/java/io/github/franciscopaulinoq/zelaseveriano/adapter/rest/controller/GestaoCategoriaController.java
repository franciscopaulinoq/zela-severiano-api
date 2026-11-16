package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/categorias")
@RequiredArgsConstructor
public class GestaoCategoriaController {

  private final CategoriaService categoriaService;

  @PostMapping
  public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaRegistroDTO dados) {
    CategoriaDTO criada = categoriaService.criar(dados);
    return ResponseEntity.status(HttpStatus.CREATED).body(criada);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoriaDTO> atualizar(
      @PathVariable("id") Integer categoriaId, @Valid @RequestBody CategoriaRegistroDTO dados) {
    CategoriaDTO atualizada = categoriaService.atualizar(categoriaId, dados);
    return ResponseEntity.ok(atualizada);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable("id") Integer categoriaId) {
    categoriaService.remover(categoriaId);
    return ResponseEntity.noContent().build();
  }
}

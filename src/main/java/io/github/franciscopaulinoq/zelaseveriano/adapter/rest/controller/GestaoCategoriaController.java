package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.CategoriaRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.CategoriaRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.CategoriaResponse;
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
    private final CategoriaRestMapper mapper;

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        var criada = categoriaService.criar(mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(criada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable("id") Integer categoriaId, @Valid @RequestBody CategoriaRequest request) {
        var atualizada = categoriaService.atualizar(categoriaId, mapper.map(request));
        return ResponseEntity.ok(mapper.map(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Integer categoriaId) {
        categoriaService.remover(categoriaId);
        return ResponseEntity.noContent().build();
    }
}

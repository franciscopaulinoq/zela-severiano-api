package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.CategoriaRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.CategoriaResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.usecase.ListarCategoriasUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ListarCategoriasUseCase listarCategoriasUseCase;
    private final CategoriaRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> response = mapper.map(listarCategoriasUseCase.execute());
        return ResponseEntity.ok(response);
    }
}
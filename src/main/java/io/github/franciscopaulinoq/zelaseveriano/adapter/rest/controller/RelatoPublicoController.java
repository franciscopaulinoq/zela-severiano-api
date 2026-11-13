package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RelatoRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoPublicoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/relatos")
@RequiredArgsConstructor
public class RelatoPublicoController {

    private final RelatoService relatoService;
    private final RelatoRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<RelatoPublicoResponse>> listar(
            @Parameter(description = "Filtra pela categoria do relato") @RequestParam(required = false)
                    Integer categoriaId) {
        return ResponseEntity.ok(mapper.mapPublico(relatoService.listarPublicos(categoriaId)));
    }
}

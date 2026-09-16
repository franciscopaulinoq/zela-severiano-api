package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RelatoRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.AtualizarStatusRelatoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoGestaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.usecase.AtualizarStatusRelatoUseCase;
import io.github.franciscopaulinoq.zelaseveriano.application.usecase.ListarTodosRelatosUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gestao/relatos")
@RequiredArgsConstructor
public class GestaoRelatoController {

    private final ListarTodosRelatosUseCase listarTodosRelatosUseCase;
    private final AtualizarStatusRelatoUseCase atualizarStatusRelatoUseCase;
    private final RelatoRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<RelatoGestaoResponse>> listarTodos() {
        return ResponseEntity.ok(mapper.mapGestao(listarTodosRelatosUseCase.execute()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable("id") Long relatoId,
            @Valid @RequestBody AtualizarStatusRelatoRequest request) {
        atualizarStatusRelatoUseCase.execute(relatoId, request.getStatus(), request.getObservacao());
        return ResponseEntity.noContent().build();
    }
}

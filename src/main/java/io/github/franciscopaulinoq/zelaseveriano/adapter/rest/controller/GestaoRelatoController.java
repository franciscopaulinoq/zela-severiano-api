package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RelatoRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.AtualizarStatusRelatoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.EstatisticasRelatosResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.HistoricoStatusResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoGestaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/relatos")
@RequiredArgsConstructor
public class GestaoRelatoController {

    private final RelatoService relatoService;
    private final RelatoRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<RelatoGestaoResponse>> listarTodos(
            @Parameter(description = "Filtra pelo status do relato") @RequestParam(required = false)
                    StatusRelato status,
            @Parameter(description = "Filtra pela categoria do relato") @RequestParam(required = false)
                    Integer categoriaId) {
        return ResponseEntity.ok(mapper.mapGestao(relatoService.listarTodos(new FiltroRelatos(status, categoriaId))));
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasRelatosResponse> obterEstatisticas() {
        return ResponseEntity.ok(mapper.mapEstatisticas(relatoService.obterEstatisticas()));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> obterHistorico(@PathVariable("id") Long relatoId) {
        return ResponseEntity.ok(mapper.mapHistorico(relatoService.obterHistoricoGestao(relatoId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable("id") Long relatoId, @Valid @RequestBody AtualizarStatusRelatoRequest request) {
        relatoService.atualizarStatus(relatoId, request.getStatus(), request.getObservacao());
        return ResponseEntity.noContent().build();
    }
}

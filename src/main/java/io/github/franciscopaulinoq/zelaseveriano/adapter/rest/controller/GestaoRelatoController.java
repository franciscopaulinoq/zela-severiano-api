package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarStatusRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.HistoricoStatusDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

  @GetMapping
  public ResponseEntity<Page<RelatoGestaoDTO>> listarTodos(
      @Parameter(description = "Filtra pelo status do relato") @RequestParam(required = false)
          StatusRelato status,
      @Parameter(description = "Filtra pela categoria do relato") @RequestParam(required = false)
          Integer categoriaId,
      @Parameter(description = "Inclui relatos criados a partir desta data (AAAA-MM-DD)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dataInicial,
      @Parameter(description = "Inclui relatos criados até esta data (AAAA-MM-DD)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dataFinal,
      @Parameter(hidden = true) Pageable pageable) {
    FiltroRelatos filtro = new FiltroRelatos(status, categoriaId, dataInicial, dataFinal);
    return ResponseEntity.ok(relatoService.listarTodos(filtro, pageable));
  }

  @GetMapping("/estatisticas")
  public ResponseEntity<EstatisticasRelatosDTO> obterEstatisticas() {
    return ResponseEntity.ok(relatoService.obterEstatisticas());
  }

  @GetMapping("/{id}/historico")
  public ResponseEntity<List<HistoricoStatusDTO>> obterHistorico(
      @PathVariable("id") Long relatoId) {
    return ResponseEntity.ok(relatoService.obterHistoricoGestao(relatoId));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> atualizarStatus(
      @PathVariable("id") Long relatoId, @Valid @RequestBody AtualizarStatusRelatoDTO dados) {
    relatoService.atualizarStatus(relatoId, dados.getStatus(), dados.getObservacao());
    return ResponseEntity.noContent().build();
  }
}

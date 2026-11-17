package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AtualizarStatusRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ComentarioRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ComentarioRelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.DetalheGestaoRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.FotoRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.HistoricoStatusDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
      @Parameter(hidden = true) Pageable pageable) {
    return ResponseEntity.ok(
        relatoService.listarTodos(new FiltroRelatos(status, categoriaId), pageable));
  }

  @GetMapping("/estatisticas")
  public ResponseEntity<EstatisticasRelatosDTO> obterEstatisticas() {
    return ResponseEntity.ok(relatoService.obterEstatisticas());
  }

  @GetMapping("/{id}/detalhe-ampliado")
  public ResponseEntity<DetalheGestaoRelatoDTO> obterDetalheAmpliado(
      @PathVariable("id") Long relatoId) {
    return ResponseEntity.ok(relatoService.obterDetalheGestao(relatoId));
  }

  @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FotoRelatoDTO> adicionarFoto(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable("id") Long relatoId,
      @RequestPart("foto") MultipartFile foto)
      throws IOException {
    FotoRelatoDTO resultado =
        relatoService.adicionarFotoGestao(
            UUID.fromString(userDetails.getUsername()),
            relatoId,
            foto.getBytes(),
            foto.getOriginalFilename(),
            foto.getContentType());
    return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
  }

  @PostMapping("/{id}/comentarios")
  public ResponseEntity<ComentarioRelatoDTO> adicionarComentario(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable("id") Long relatoId,
      @Valid @RequestBody ComentarioRelatoRegistroDTO dados) {
    ComentarioRelatoDTO resultado =
        relatoService.adicionarComentarioGestao(
            UUID.fromString(userDetails.getUsername()), relatoId, dados.texto());
    return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
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

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoRelatoResultadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ImportacaoRelatoLegadoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/gestao/relatos/importacao")
@RequiredArgsConstructor
public class GestaoImportacaoController {

  private final ImportacaoRelatoLegadoService service;

  @Operation(
      summary = "Importa relatos legados em CSV",
      description =
          "Arquivo UTF-8 de até 10 MB com o cabeçalho identificador_externo,cpf,categoria_id,"
              + "descricao,latitude,longitude,status,criado_em. A data usa ISO-8601 com fuso.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportacaoRelatoResultadoDTO> importar(
      @RequestPart("arquivo") MultipartFile arquivo) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.importar(arquivo));
  }
}

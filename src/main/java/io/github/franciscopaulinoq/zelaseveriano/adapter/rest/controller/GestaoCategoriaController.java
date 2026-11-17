package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoCategoriaResultadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/gestao/categorias")
@RequiredArgsConstructor
public class GestaoCategoriaController {

  private static final long LIMITE_IMPORTACAO_BYTES = 256 * 1024;

  private final CategoriaService categoriaService;

  @PostMapping
  public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaRegistroDTO dados) {
    CategoriaDTO criada = categoriaService.criar(dados);
    return ResponseEntity.status(HttpStatus.CREATED).body(criada);
  }

  @PostMapping(value = "/importacao", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportacaoCategoriaResultadoDTO> importar(
      @Parameter(description = "CSV UTF-8 com cabeçalho nome,icone e limite de 256 KiB")
          @RequestPart("arquivo")
          MultipartFile arquivo)
      throws IOException {
    if (arquivo.isEmpty() || arquivo.getSize() > LIMITE_IMPORTACAO_BYTES) {
      throw new IllegalArgumentException("O arquivo CSV deve ter entre 1 byte e 256 KiB.");
    }
    try (Reader leitor = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8)) {
      return ResponseEntity.ok(categoriaService.importar(leitor));
    }
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

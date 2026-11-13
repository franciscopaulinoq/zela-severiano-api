package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RelatoRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RelatoRegistroRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoCriadoResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoDetalheResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoListagemResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/relatos")
@RequiredArgsConstructor
public class RelatoController {

    private final RelatoService relatoService;
    private final RelatoRestMapper mapper;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RelatoCriadoResponse> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("dados") RelatoRegistroRequest request,
            @RequestPart(value = "foto", required = false) MultipartFile foto)
            throws IOException {

        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        RelatoRegistroDTO dto = mapper.map(request);

        byte[] fileBytes = (foto != null) ? foto.getBytes() : null;
        String originalFilename = (foto != null) ? foto.getOriginalFilename() : null;
        String contentType = (foto != null) ? foto.getContentType() : null;

        var resultado = relatoService.registrar(usuarioId, dto, fileBytes, originalFilename, contentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(resultado));
    }

    @GetMapping
    @Parameter(name = "pagina", in = ParameterIn.QUERY, description = "Número da página, começando em 0")
    @Parameter(name = "tamanho", in = ParameterIn.QUERY, description = "Itens por página (padrão 10, máximo 50)")
    public ResponseEntity<Page<RelatoListagemResponse>> listarMeusRelatos(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(description = "Filtra pelo status do relato") @RequestParam(required = false)
                    StatusRelato status,
            @Parameter(description = "Filtra pela categoria do relato") @RequestParam(required = false)
                    Integer categoriaId) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        FiltroRelatos filtro = new FiltroRelatos(status, categoriaId);
        return ResponseEntity.ok(
                relatoService.visualizarMeus(usuarioId, filtro, pageable).map(mapper::mapListagem));
    }

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<Void> cancelar(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long relatoId) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        relatoService.cancelar(usuarioId, relatoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatoDetalheResponse> obterDetalhe(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long relatoId) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(mapper.mapDetalhe(relatoService.obterDetalhe(usuarioId, relatoId)));
    }
}

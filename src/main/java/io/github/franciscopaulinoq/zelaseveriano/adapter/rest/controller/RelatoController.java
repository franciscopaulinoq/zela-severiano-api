package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RelatoRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RelatoRegistroRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoCriadoResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoDetalheResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoListagemResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relatos")
@RequiredArgsConstructor
public class RelatoController {

    private final RelatoService relatoService;
    private final RelatoRestMapper mapper;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<RelatoCriadoResponse> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("dados") RelatoRegistroRequest request,
            @RequestPart(value = "foto", required = false) MultipartFile foto) throws IOException {

        System.out.println("A FOTO CHEGOU NO CONTROLLER? -> " + (foto != null));

        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        RelatoRegistroDTO dto = mapper.map(request);

        byte[] fileBytes = (foto != null) ? foto.getBytes() : null;
        String originalFilename = (foto != null) ? foto.getOriginalFilename() : null;
        String contentType = (foto != null) ? foto.getContentType() : null;

        var resultado = relatoService.registrar(usuarioId, dto, fileBytes, originalFilename, contentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(resultado));
    }

    @GetMapping
    public ResponseEntity<List<RelatoListagemResponse>> listarMeusRelatos(@AuthenticationPrincipal UserDetails userDetails) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(mapper.mapListagem(relatoService.visualizarMeus(usuarioId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatoDetalheResponse> obterDetalhe(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long relatoId) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(mapper.mapDetalhe(relatoService.obterDetalhe(usuarioId, relatoId)));
    }
}

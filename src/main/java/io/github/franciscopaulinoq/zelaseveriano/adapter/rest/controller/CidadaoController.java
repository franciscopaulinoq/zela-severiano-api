package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.RegistrarCidadaoMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RegistrarCidadaoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RegistrarCidadaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.usecase.RegistrarCidadaoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cidadaos/res")
@RequiredArgsConstructor
public class CidadaoController {

    private final RegistrarCidadaoUseCase registrarCidadaoUseCase;
    private final RegistrarCidadaoMapper mapper;

    @PostMapping
    public ResponseEntity<RegistrarCidadaoResponse> registrar(
            @Valid @RequestBody RegistrarCidadaoRequest request) {

        RegistrarCidadaoDTO dto = mapper.map(request);

        RegistrarCidadaoDTO resultado = registrarCidadaoUseCase.execute(dto, request.getSenha());

        RegistrarCidadaoResponse response = mapper.map(resultado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.PerfilRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.PerfilMeResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.service.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/perfis")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;
    private final PerfilRestMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<PerfilMeResponse> obterMeuPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        var perfil = perfilService.obterMeuPerfil(usuarioId);
        return ResponseEntity.ok(mapper.map(perfil));
    }
}

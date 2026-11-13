package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper.PerfilRestMapper;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.TrocarSenhaRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.PerfilMeResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.PerfilService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfis")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;
    private final ContaService contaService;
    private final PerfilRestMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<PerfilMeResponse> obterMeuPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        var perfil = perfilService.obterMeuPerfil(usuarioId);
        return ResponseEntity.ok(mapper.map(perfil));
    }

    @PatchMapping("/me/senha")
    public ResponseEntity<Void> trocarSenha(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody TrocarSenhaRequest request) {
        UUID usuarioId = UUID.fromString(userDetails.getUsername());
        contaService.trocarSenha(usuarioId, request.getSenhaAtual(), request.getNovaSenha());
        return ResponseEntity.noContent().build();
    }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.LoginRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RenovarSessaoRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.LoginResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.SessaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ContaService contaService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SessaoDTO sessao = contaService.autenticar(request.getCpf(), request.getSenha());
        return ResponseEntity.ok(new LoginResponse(sessao.tokenAcesso(), sessao.tokenRenovacao()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> renovarSessao(@Valid @RequestBody RenovarSessaoRequest request) {
        String token = contaService.renovarSessao(request.getRefreshToken());
        return ResponseEntity.ok(new LoginResponse(token, null));
    }
}

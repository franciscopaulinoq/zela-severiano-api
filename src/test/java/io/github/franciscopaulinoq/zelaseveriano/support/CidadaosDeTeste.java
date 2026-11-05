package io.github.franciscopaulinoq.zelaseveriano.support;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class CidadaosDeTeste {

    public static final String SENHA = "senha-de-teste";

    private final ContaService contaService;

    public Cidadao cadastrar(String nomeCompleto) {
        String cpf = CpfsDeTeste.novo();
        RegistrarCidadaoDTO dto = RegistrarCidadaoDTO.builder()
                .cpf(cpf)
                .nomeCompleto(nomeCompleto)
                .email("cidadao@example.com")
                .logradouro("Rua Principal")
                .numero("10")
                .bairro("Centro")
                .build();
        RegistrarCidadaoDTO registrado = contaService.registrarCidadao(dto, SENHA);
        return new Cidadao(registrado.getUsuarioId(), registrado.getPerfilId(), cpf);
    }

    public String token(Cidadao cidadao) {
        return contaService.autenticar(cidadao.cpf(), SENHA);
    }

    public record Cidadao(UUID usuarioId, UUID perfilId, String cpf) {}
}

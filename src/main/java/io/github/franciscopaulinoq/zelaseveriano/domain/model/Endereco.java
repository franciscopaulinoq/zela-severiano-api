package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class Endereco {
    private UUID id;
    private Perfil perfil;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private OffsetDateTime criadoEm;
}

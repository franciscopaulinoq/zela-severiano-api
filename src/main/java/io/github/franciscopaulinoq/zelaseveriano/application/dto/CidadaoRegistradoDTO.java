package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import java.util.UUID;

public record CidadaoRegistradoDTO(
    UUID usuarioId,
    UUID perfilId,
    UUID enderecoId,
    String cpf,
    String nomeCompleto,
    String email,
    String mensagem) {}

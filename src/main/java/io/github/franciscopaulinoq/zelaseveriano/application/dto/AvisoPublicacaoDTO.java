package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record AvisoPublicacaoDTO(
    @NotBlank @Size(max = 150) String titulo,
    @NotBlank @Size(max = 5000) String conteudo,
    @NotEmpty @Size(max = 500) List<@NotNull UUID> destinatarios) {}

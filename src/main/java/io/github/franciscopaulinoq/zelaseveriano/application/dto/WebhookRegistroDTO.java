package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookRegistroDTO(
    @NotBlank(message = "URL é obrigatória")
        @Size(max = 2048, message = "URL deve ter no máximo 2048 caracteres")
        String url) {}

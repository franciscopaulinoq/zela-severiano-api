package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarEmailDTO(
    @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "\\d{6}", message = "Código deve ter seis dígitos")
        String codigo) {}

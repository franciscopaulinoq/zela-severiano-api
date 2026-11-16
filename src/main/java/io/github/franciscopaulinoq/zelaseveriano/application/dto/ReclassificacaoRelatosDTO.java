package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(description = "Reclassificação de 1 a 100 relatos; IDs repetidos são processados uma vez")
public record ReclassificacaoRelatosDTO(
    @NotEmpty(message = "Informe ao menos um relato")
        @Size(max = 100, message = "Informe no máximo 100 relatos")
        Set<
                @NotNull(message = "O ID do relato é obrigatório")
                @Positive(message = "O ID do relato deve ser positivo") Long>
            relatoIds,
    @NotNull(message = "A categoria é obrigatória")
        @Positive(message = "A categoria deve ter um ID positivo")
        Integer categoriaId) {}

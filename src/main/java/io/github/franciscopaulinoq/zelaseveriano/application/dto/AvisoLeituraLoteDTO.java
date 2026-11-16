package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AvisoLeituraLoteDTO(@NotEmpty @Size(max = 100) List<@NotNull Long> avisoIds) {}

package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import java.util.List;

public record DetalheGestaoRelatoDTO(
    Long id,
    String descricao,
    StatusRelato status,
    String urlFoto,
    OffsetDateTime criadoEm,
    List<FotoRelatoDTO> fotos,
    List<ComentarioRelatoDTO> comentarios) {}

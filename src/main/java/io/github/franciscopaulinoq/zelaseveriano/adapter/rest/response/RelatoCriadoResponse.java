package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatoCriadoResponse {
    private Long id;
    private StatusRelato status;
    private String urlFoto;
    private OffsetDateTime criadoEm;
}
package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarStatusRelatoRequest {

    @NotNull(message = "Status é obrigatório")
    private StatusRelato status;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;
}

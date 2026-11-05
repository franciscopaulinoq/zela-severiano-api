package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenovarSessaoRequest {
    @NotBlank(message = "Token de renovação é obrigatório")
    private String refreshToken;
}

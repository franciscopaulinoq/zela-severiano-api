package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SessaoDTO(
    @JsonProperty("token") String tokenAcesso,
    @JsonProperty("refreshToken") @JsonInclude(JsonInclude.Include.NON_NULL)
        String tokenRenovacao) {}

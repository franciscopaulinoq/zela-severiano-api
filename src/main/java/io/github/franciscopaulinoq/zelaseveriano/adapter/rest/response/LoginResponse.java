package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String refreshToken;
}

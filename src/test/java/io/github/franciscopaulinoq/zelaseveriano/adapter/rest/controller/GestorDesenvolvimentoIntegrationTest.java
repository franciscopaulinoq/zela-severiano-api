package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class GestorDesenvolvimentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void gestorDeDesenvolvimentoAcessaAsRotasDeGestao() throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "11144477735", "senha": "gestor123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = JsonPath.read(corpo, "$.token");

        mockMvc.perform(get("/api/v1/gestao/relatos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}

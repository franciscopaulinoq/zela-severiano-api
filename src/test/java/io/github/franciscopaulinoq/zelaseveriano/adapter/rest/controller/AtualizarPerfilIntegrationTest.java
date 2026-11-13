package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class AtualizarPerfilIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private String token;

    @BeforeEach
    void setUp() {
        token = cidadaos.token(cidadaos.cadastrar("Helena Costa"));
    }

    @Test
    void atualizaSoONomeEMantemOEmail() throws Exception {
        mockMvc.perform(patch("/api/v1/perfis/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomeCompleto": "Helena Costa Lima"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Helena Costa Lima"))
                .andExpect(jsonPath("$.email").value("cidadao@example.com"));

        mockMvc.perform(get("/api/v1/perfis/me").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.nomeCompleto").value("Helena Costa Lima"));
    }

    @Test
    void emailInvalidoEhRecusadoComErroDeValidacao() throws Exception {
        mockMvc.perform(patch("/api/v1/perfis/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nao-e-um-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.email").exists());

        mockMvc.perform(get("/api/v1/perfis/me").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.email").value("cidadao@example.com"));
    }
}

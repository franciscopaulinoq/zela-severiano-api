package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class TrocaSenhaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private Cidadao cidadao;
    private String token;

    @BeforeEach
    void setUp() {
        cidadao = cidadaos.cadastrar("Marcos Dantas");
        token = cidadaos.token(cidadao);
    }

    @Test
    void comASenhaAtualCorretaANovaSenhaPassaAValerNoLogin() throws Exception {
        trocarSenha("""
                        {"senhaAtual": "%s", "novaSenha": "nova-senha-123"}
                        """.formatted(CidadaosDeTeste.SENHA)).andExpect(status().isNoContent());

        login("nova-senha-123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
        login(CidadaosDeTeste.SENHA).andExpect(status().isUnprocessableContent());
    }

    @Test
    void comASenhaAtualIncorretaNadaMuda() throws Exception {
        trocarSenha("""
                        {"senhaAtual": "senha-errada", "novaSenha": "nova-senha-123"}
                        """).andExpect(status().isUnprocessableContent());

        login(CidadaosDeTeste.SENHA).andExpect(status().isOk());
        login("nova-senha-123").andExpect(status().isUnprocessableContent());
    }

    @Test
    void novaSenhaCurtaEhRecusada() throws Exception {
        trocarSenha("""
                        {"senhaAtual": "%s", "novaSenha": "123"}
                        """.formatted(CidadaosDeTeste.SENHA)).andExpect(status().isBadRequest());
    }

    private ResultActions trocarSenha(String corpo) throws Exception {
        return mockMvc.perform(patch("/api/v1/perfis/me/senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private ResultActions login(String senha) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"cpf": "%s", "senha": "%s"}
                        """.formatted(cidadao.cpf(), senha)));
    }
}

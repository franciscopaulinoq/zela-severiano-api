package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoPublicoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    @BeforeEach
    void setUp() {
        Cidadao cidadao = cidadaos.cadastrar("Lúcia Freitas");
        relatoService.registrar(
                cidadao.usuarioId(), new RelatoRegistroDTO(2, "Esgoto a céu aberto", -6.08, -38.38), null, null, null);
        relatoService.registrar(
                cidadao.usuarioId(), new RelatoRegistroDTO(5, "Árvore caída", -6.09, -38.37), null, null, null);
    }

    @Test
    void listagemPublicaNaoExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/publico/relatos")).andExpect(status().isOk());
    }

    @Test
    void filtraPorCategoria() throws Exception {
        mockMvc.perform(get("/api/v1/publico/relatos").param("categoriaId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categoriaNome", everyItem(is("Outro problema"))));
    }

    @Test
    void respostaNaoExpoeDadosDoCidadaoNemDaFoto() throws Exception {
        mockMvc.perform(get("/api/v1/publico/relatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaNome").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].criadoEm").exists())
                .andExpect(jsonPath("$[*].id").doesNotExist())
                .andExpect(jsonPath("$[*].cidadaoNome").doesNotExist())
                .andExpect(jsonPath("$[*].perfil").doesNotExist())
                .andExpect(jsonPath("$[*].perfilId").doesNotExist())
                .andExpect(jsonPath("$[*].urlFoto").doesNotExist())
                .andExpect(jsonPath("$[*].descricao").doesNotExist());
    }
}

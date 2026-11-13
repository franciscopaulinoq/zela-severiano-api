package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
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
class RelatoCancelamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private Cidadao cidadao;
    private String token;

    @BeforeEach
    void setUp() {
        cidadao = cidadaos.cadastrar("Rita Gomes");
        token = cidadaos.token(cidadao);
    }

    @Test
    void cidadaoCancelaRelatoProprioPendente() throws Exception {
        Long relatoId = registrar();

        mockMvc.perform(patch("/api/v1/relatos/{id}/cancelamento", relatoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/relatos/{id}", relatoId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void relatoEmAnaliseNaoPodeSerCancelado() throws Exception {
        Long relatoId = registrar();
        relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);

        mockMvc.perform(patch("/api/v1/relatos/{id}/cancelamento", relatoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/relatos/{id}", relatoId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    private Long registrar() {
        return relatoService
                .registrar(cidadao.usuarioId(), new RelatoRegistroDTO(2, "Vazamento", -6.08, -38.38), null, null, null)
                .getId();
    }
}

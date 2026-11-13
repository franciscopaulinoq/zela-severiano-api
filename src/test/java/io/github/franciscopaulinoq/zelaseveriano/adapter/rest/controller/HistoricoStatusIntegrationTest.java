package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class HistoricoStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private Cidadao cidadao;
    private Long relatoId;

    @BeforeEach
    void setUp() {
        cidadao = cidadaos.cadastrar("Fábio Moura");
        relatoId = relatoService
                .registrar(
                        cidadao.usuarioId(),
                        new RelatoRegistroDTO(1, "Poste piscando", -6.08, -38.38),
                        null,
                        null,
                        null)
                .getId();
        relatoService.atualizarStatus(relatoId, StatusRelato.EM_ANALISE, null);
        relatoService.atualizarStatus(relatoId, StatusRelato.CONCLUIDO, "Reator substituído");
    }

    @Test
    void donoDoRelatoVeAsMudancasEmOrdemCronologica() throws Exception {
        mockMvc.perform(get("/api/v1/relatos/{id}/historico", relatoId)
                        .header("Authorization", "Bearer " + cidadaos.token(cidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].statusNovo").value("EM_ANALISE"))
                .andExpect(jsonPath("$[1].statusNovo").value("CONCLUIDO"))
                .andExpect(jsonPath("$[1].observacao").value("Reator substituído"))
                .andExpect(jsonPath("$[1].alteradoEm").exists());
    }

    @Test
    void gestorConsultaOHistoricoDeQualquerRelato() throws Exception {
        mockMvc.perform(get("/api/v1/gestao/relatos/{id}/historico", relatoId)
                        .header("Authorization", "Bearer " + cidadaos.tokenDeNovoGestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void outroCidadaoNaoVeOHistorico() throws Exception {
        String outro = cidadaos.token(cidadaos.cadastrar("Gilda Ramos"));

        mockMvc.perform(get("/api/v1/relatos/{id}/historico", relatoId).header("Authorization", "Bearer " + outro))
                .andExpect(status().isUnprocessableContent());
    }
}

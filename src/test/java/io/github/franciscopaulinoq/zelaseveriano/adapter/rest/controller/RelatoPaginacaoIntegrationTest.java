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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoPaginacaoIntegrationTest {

    private static final int TOTAL_DE_RELATOS = 23;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private String token;
    private final List<Long> idsDoMaisNovoParaOMaisAntigo = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Cidadao cidadao = cidadaos.cadastrar("Carlos Lima");
        token = cidadaos.token(cidadao);
        for (int i = 0; i < TOTAL_DE_RELATOS; i++) {
            Long id = relatoService
                    .registrar(
                            cidadao.usuarioId(),
                            new RelatoRegistroDTO(1, "Relato " + i, -6.08, -38.38),
                            null,
                            null,
                            null)
                    .getId();
            relatoService.atualizarStatus(id, StatusRelato.EM_ANALISE, null);
            idsDoMaisNovoParaOMaisAntigo.addFirst(id);
        }
    }

    @Test
    void primeiraPaginaTrazOsMaisRecentesEOsMetadados() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("pagina", "0")
                        .param("tamanho", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.content[0].id").value(idsDoMaisNovoParaOMaisAntigo.getFirst()))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(TOTAL_DE_RELATOS))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void ultimaPaginaVemParcial() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("pagina", "2")
                        .param("tamanho", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[2].id").value(idsDoMaisNovoParaOMaisAntigo.getLast()));
    }

    @Test
    void paginaAlemDoTotalVemVazia() throws Exception {
        mockMvc.perform(get("/api/v1/relatos").param("pagina", "7").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page.totalElements").value(TOTAL_DE_RELATOS));
    }

    @Test
    void semParametrosUsaPaginaZeroComDezItens() throws Exception {
        mockMvc.perform(get("/api/v1/relatos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.content", hasSize(10)));
    }

    @Test
    void tamanhoAcimaDoLimiteFicaNoMaximo() throws Exception {
        mockMvc.perform(get("/api/v1/relatos").param("tamanho", "500").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(50))
                .andExpect(jsonPath("$.content", hasSize(TOTAL_DE_RELATOS)));
    }
}

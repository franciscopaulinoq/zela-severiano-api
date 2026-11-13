package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
class RelatoFiltrosIntegrationTest {

    private static final int ILUMINACAO = 1;
    private static final int LIMPEZA = 3;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private String tokenCidadao;
    private String tokenGestor;
    private Long iluminacaoPendente;
    private Long iluminacaoEmAnalise;
    private Long limpezaEmAnalise;

    @BeforeEach
    void setUp() {
        Cidadao cidadao = cidadaos.cadastrar("Ana Souza");
        tokenCidadao = cidadaos.token(cidadao);
        tokenGestor = cidadaos.tokenDeNovoGestor();

        iluminacaoPendente = registrar(cidadao, ILUMINACAO);
        iluminacaoEmAnalise = registrar(cidadao, ILUMINACAO);
        limpezaEmAnalise = registrar(cidadao, LIMPEZA);
        relatoService.atualizarStatus(iluminacaoEmAnalise, StatusRelato.EM_ANALISE, null);
        relatoService.atualizarStatus(limpezaEmAnalise, StatusRelato.EM_ANALISE, null);
    }

    @Test
    void cidadaoFiltraPorStatus() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("status", "EM_ANALISE")
                        .header("Authorization", bearer(tokenCidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[*].id",
                        containsInAnyOrder(iluminacaoEmAnalise.intValue(), limpezaEmAnalise.intValue())))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void cidadaoFiltraPorCategoria() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("categoriaId", String.valueOf(ILUMINACAO))
                        .header("Authorization", bearer(tokenCidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[*].id",
                        containsInAnyOrder(iluminacaoPendente.intValue(), iluminacaoEmAnalise.intValue())));
    }

    @Test
    void cidadaoCombinaOsDoisFiltros() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("status", "EM_ANALISE")
                        .param("categoriaId", String.valueOf(ILUMINACAO))
                        .header("Authorization", bearer(tokenCidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(iluminacaoEmAnalise));
    }

    @Test
    void categoriaInexistenteDevolveListaVazia() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("categoriaId", "9999")
                        .header("Authorization", bearer(tokenCidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void statusInvalidoEhRecusadoComoParametroInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/relatos")
                        .param("status", "QUALQUER")
                        .header("Authorization", bearer(tokenCidadao)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gestorCombinaOsDoisFiltros() throws Exception {
        mockMvc.perform(get("/api/v1/gestao/relatos")
                        .param("status", "EM_ANALISE")
                        .param("categoriaId", String.valueOf(LIMPEZA))
                        .header("Authorization", bearer(tokenGestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", limpezaEmAnalise).exists())
                .andExpect(jsonPath("$[*].status", everyItem(is("EM_ANALISE"))))
                .andExpect(jsonPath("$[?(@.id == %d)]", iluminacaoEmAnalise).doesNotExist());
    }

    private Long registrar(Cidadao cidadao, int categoriaId) {
        return relatoService
                .registrar(
                        cidadao.usuarioId(),
                        new RelatoRegistroDTO(categoriaId, "Relato de teste", -6.08, -38.38),
                        null,
                        null,
                        null)
                .getId();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}

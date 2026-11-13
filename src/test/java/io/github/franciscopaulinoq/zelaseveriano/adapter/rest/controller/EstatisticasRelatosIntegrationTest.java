package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class EstatisticasRelatosIntegrationTest {

    private static final int AGUA_ESGOTO = 2;
    private static final int ASFALTO = 4;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    @Test
    void contagensAcompanhamOsRelatosCadastrados() throws Exception {
        String token = cidadaos.tokenDeNovoGestor();
        DocumentContext antes = estatisticas(token);

        Cidadao cidadao = cidadaos.cadastrar("Elisa Barros");
        registrar(cidadao, AGUA_ESGOTO);
        Long emAnalise = registrar(cidadao, ASFALTO);
        relatoService.atualizarStatus(emAnalise, StatusRelato.EM_ANALISE, null);
        DocumentContext depois = estatisticas(token);

        assertThat(numero(depois, "$.total") - numero(antes, "$.total")).isEqualTo(2);
        assertThat(numero(depois, "$.porStatus.PENDENTE") - numero(antes, "$.porStatus.PENDENTE"))
                .isEqualTo(1);
        assertThat(numero(depois, "$.porStatus.EM_ANALISE") - numero(antes, "$.porStatus.EM_ANALISE"))
                .isEqualTo(1);
        assertThat(totalDaCategoria(depois, AGUA_ESGOTO) - totalDaCategoria(antes, AGUA_ESGOTO))
                .isEqualTo(1);
        assertThat(totalDaCategoria(depois, ASFALTO) - totalDaCategoria(antes, ASFALTO))
                .isEqualTo(1);
    }

    @Test
    void cidadaoNaoAcessaAsEstatisticas() throws Exception {
        String tokenCidadao = cidadaos.token(cidadaos.cadastrar("Nair Torres"));

        mockMvc.perform(get("/api/v1/gestao/relatos/estatisticas").header("Authorization", "Bearer " + tokenCidadao))
                .andExpect(status().isForbidden());
    }

    private DocumentContext estatisticas(String token) throws Exception {
        String corpo = mockMvc.perform(
                        get("/api/v1/gestao/relatos/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.parse(corpo);
    }

    private static long numero(DocumentContext json, String caminho) {
        return json.read(caminho, Long.class);
    }

    private static long totalDaCategoria(DocumentContext json, int categoriaId) {
        return json.read("$.porCategoria[?(@.categoriaId == " + categoriaId + ")].total", Long[].class)[0];
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
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoCategoriaIntegrationTest {

    private static final String CATEGORIA = """
            {"nome": "Calçadas", "iconeNome": "person-walking"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private CidadaosDeTeste cidadaos;

    private String tokenGestor;

    @BeforeEach
    void setUp() {
        tokenGestor = cidadaos.tokenDeNovoGestor();
    }

    @Test
    void gestorCriaAtualizaERemoveCategoria() throws Exception {
        Integer id = criarCategoria();

        mockMvc.perform(put("/api/v1/gestao/categorias/{id}", id)
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Calçadas e passeios", "iconeNome": "person-walking"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Calçadas e passeios"));

        mockMvc.perform(delete("/api/v1/gestao/categorias/{id}", id).header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isNoContent());
    }

    @Test
    void cidadaoNaoEscreveEmCategorias() throws Exception {
        String tokenCidadao = cidadaos.token(cidadaos.cadastrar("Otávio Reis"));

        mockMvc.perform(post("/api/v1/gestao/categorias")
                        .header("Authorization", "Bearer " + tokenCidadao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORIA))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/gestao/categorias/{id}", 1).header("Authorization", "Bearer " + tokenCidadao))
                .andExpect(status().isForbidden());
    }

    @Test
    void categoriaComRelatoVinculadoNaoPodeSerRemovida() throws Exception {
        Integer id = criarCategoria();
        Cidadao cidadao = cidadaos.cadastrar("Sônia Prado");
        relatoService.registrar(
                cidadao.usuarioId(), new RelatoRegistroDTO(id, "Calçada quebrada", -6.08, -38.38), null, null, null);

        mockMvc.perform(delete("/api/v1/gestao/categorias/{id}", id).header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isConflict());
    }

    @Test
    void nomeEmBrancoEhRecusado() throws Exception {
        mockMvc.perform(post("/api/v1/gestao/categorias")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": " ", "iconeNome": "x"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private Integer criarCategoria() throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/gestao/categorias")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORIA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }
}

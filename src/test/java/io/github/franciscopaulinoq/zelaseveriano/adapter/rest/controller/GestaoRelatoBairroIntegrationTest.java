package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoRelatoBairroIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private RelatoService relatoService;

  @Autowired private CategoriaService categoriaService;

  @Autowired private CidadaosDeTeste cidadaos;

  private Cidadao autorDoCentro;
  private Cidadao autorDaBoaVista;
  private String tokenGestor;
  private Integer categoriaId;
  private Long relatoDoCentro;
  private Long relatoDaBoaVista;

  @BeforeEach
  void setUp() {
    autorDoCentro = cidadaos.cadastrar("Ana Souza", "Centro");
    autorDaBoaVista = cidadaos.cadastrar("Bruno Lima", "Boa Vista");
    tokenGestor = cidadaos.tokenDeNovoGestor();
    categoriaId =
        categoriaService
            .criar(new CategoriaRegistroDTO("Bairros " + UUID.randomUUID(), "bairro"))
            .getId();
    relatoDoCentro = registrar(autorDoCentro, categoriaId);
    relatoDaBoaVista = registrar(autorDaBoaVista, categoriaId);
  }

  @Test
  void listagemApresentaOBairroDoAutorDeCadaRelato() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos")
                .param("categoriaId", categoriaId.toString())
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(
            jsonPath("$.content[?(@.id == %d)].bairro", relatoDoCentro).value(contains("Centro")))
        .andExpect(
            jsonPath("$.content[?(@.id == %d)].bairro", relatoDaBoaVista)
                .value(contains("Boa Vista")));
  }

  @Test
  void filtrosMantemOBairroDoRelatoRetornado() throws Exception {
    Long relatoFiltrado = registrar(autorDaBoaVista, categoriaId);
    relatoService.atualizarStatus(relatoFiltrado, StatusRelato.EM_ANALISE, null);

    mockMvc
        .perform(
            get("/api/v1/gestao/relatos")
                .param("status", "EM_ANALISE")
                .param("categoriaId", categoriaId.toString())
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(relatoFiltrado))
        .andExpect(jsonPath("$.content[0].bairro").value("Boa Vista"));
  }

  @Test
  void bairroEMetadadosSaoPreservadosNaPaginacao() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos")
                .param("categoriaId", categoriaId.toString())
                .param("tamanho", "1")
                .param("pagina", "1")
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.page.totalElements").value(2))
        .andExpect(jsonPath("$.page.totalPages").value(2))
        .andExpect(jsonPath("$.content[0].id").value(relatoDoCentro))
        .andExpect(jsonPath("$.content[0].bairro").value("Centro"));
  }

  @Test
  void cidadaoNaoAcessaAListagemDeGestao() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos")
                .header("Authorization", bearer(cidadaos.token(autorDoCentro))))
        .andExpect(status().isForbidden());
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

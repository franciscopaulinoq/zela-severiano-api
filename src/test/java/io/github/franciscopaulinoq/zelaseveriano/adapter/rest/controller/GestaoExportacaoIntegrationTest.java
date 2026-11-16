package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoExportacaoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private RelatoService relatoService;
  @Autowired private CategoriaService categoriaService;

  private Cidadao cidadao;
  private int categoriaId;
  private String categoriaNome;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Maria, da \"Silva\"");
    categoriaNome = "Exportação " + UUID.randomUUID();
    categoriaId =
        categoriaService.criar(new CategoriaRegistroDTO(categoriaNome, "arquivo")).getId();
  }

  @Test
  void exportaCabecalhoCamposECaracteresEspeciais() throws Exception {
    Long id = registrar(categoriaId, "Buraco, \"grande\"\nna rua");

    String csv =
        exportar(null, categoriaId)
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8"))
            .andExpect(
                header()
                    .string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("relatos.csv")))
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<List<String>> linhas = lerCsv(csv);

    assertThat(linhas).hasSize(2);
    assertThat(linhas.getFirst())
        .containsExactly(
            "id", "autor", "categoria", "status", "descricao", "observacao", "criado_em");
    assertThat(linhas.get(1).subList(0, 5))
        .containsExactly(
            id.toString(),
            "Maria, da \"Silva\"",
            categoriaNome,
            "PENDENTE",
            "Buraco, \"grande\"\nna rua");
    assertThat(linhas.get(1).get(6)).isNotBlank();
  }

  @Test
  void exportaUmaLinhaPorRelatoComObservacaoDaAvaliacao() throws Exception {
    Long primeiro = registrar(categoriaId, "Descrição simples");
    Long avaliado = registrar(categoriaId, "Relato avaliado");
    String observacao = "Observação, \"final\"\nliberada";
    relatoService.atualizarStatus(avaliado, StatusRelato.EM_ANALISE, observacao);

    String csv =
        exportar(null, categoriaId)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<List<String>> linhas = lerCsv(csv);

    assertThat(linhas).hasSize(3);
    assertThat(linhas.get(1).getFirst()).isEqualTo(avaliado.toString());
    assertThat(linhas.get(1).get(3)).isEqualTo("EM_ANALISE");
    assertThat(linhas.get(1).get(4)).isEqualTo("Relato avaliado");
    assertThat(linhas.get(1).get(5)).isEqualTo(observacao);
    assertThat(linhas.get(1).get(6)).isNotBlank();
    assertThat(linhas.get(2).getFirst()).isEqualTo(primeiro.toString());
    assertThat(linhas.get(2).get(4)).isEqualTo("Descrição simples");
  }

  @Test
  void aplicaStatusECategoriaAoMesmoTempo() throws Exception {
    Long esperado = registrar(categoriaId, "Pendente da categoria");
    Long emAnalise = registrar(categoriaId, "Em análise da categoria");
    relatoService.atualizarStatus(emAnalise, StatusRelato.EM_ANALISE, null);
    int outraCategoria =
        categoriaService
            .criar(new CategoriaRegistroDTO("Outra " + UUID.randomUUID(), "arquivo"))
            .getId();
    registrar(outraCategoria, "Pendente de outra categoria");

    String csv =
        exportar(StatusRelato.PENDENTE, categoriaId)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(lerCsv(csv)).hasSize(2);
    assertThat(lerCsv(csv).get(1).getFirst()).isEqualTo(esperado.toString());
  }

  @Test
  void exportacaoVaziaContemSomenteCabecalho() throws Exception {
    String csv =
        exportar(null, categoriaId)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(lerCsv(csv))
        .containsExactly(
            List.of("id", "autor", "categoria", "status", "descricao", "observacao", "criado_em"));
  }

  @Test
  void cidadaoNaoPodeExportar() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/exportacao")
                .header("Authorization", "Bearer " + cidadaos.token(cidadao)))
        .andExpect(status().isForbidden());
  }

  private Long registrar(int categoria, String descricao) {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(categoria, descricao, -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private ResultActions exportar(StatusRelato status, Integer categoria) throws Exception {
    MockHttpServletRequestBuilder requisicao =
        get("/api/v1/gestao/relatos/exportacao")
            .with(
                org.springframework.security.test.web.servlet.request
                    .SecurityMockMvcRequestPostProcessors.user("gestor")
                    .roles("GESTOR"))
            .queryParam("categoriaId", categoria.toString());
    if (status != null) {
      requisicao.queryParam("status", status.name());
    }
    MvcResult pendente = mockMvc.perform(requisicao).andReturn();
    return mockMvc.perform(asyncDispatch(pendente));
  }

  private List<List<String>> lerCsv(String csv) {
    List<List<String>> linhas = new ArrayList<>();
    List<String> linha = new ArrayList<>();
    StringBuilder campo = new StringBuilder();
    boolean aspas = false;
    for (int indice = 0; indice < csv.length(); indice++) {
      char atual = csv.charAt(indice);
      if (atual == '"') {
        if (aspas && indice + 1 < csv.length() && csv.charAt(indice + 1) == '"') {
          campo.append('"');
          indice++;
        } else {
          aspas = !aspas;
        }
      } else if (atual == ',' && !aspas) {
        linha.add(campo.toString());
        campo.setLength(0);
      } else if (atual == '\n' && !aspas) {
        linha.add(campo.toString());
        campo.setLength(0);
        linhas.add(linha);
        linha = new ArrayList<>();
      } else if (atual != '\r' || aspas) {
        campo.append(atual);
      }
    }
    return linhas;
  }
}

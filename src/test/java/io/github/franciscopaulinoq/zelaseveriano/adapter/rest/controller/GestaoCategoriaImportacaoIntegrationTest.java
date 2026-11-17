package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoCategoriaImportacaoIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CategoriaService categoriaService;

  @Autowired private CidadaosDeTeste cidadaos;

  private String tokenGestor;

  @BeforeEach
  void setUp() {
    tokenGestor = cidadaos.tokenDeNovoGestor();
  }

  @Test
  void importaCategoriasValidas() throws Exception {
    String primeira = "Praças " + UUID.randomUUID();
    String segunda = "Vias, pontes " + UUID.randomUUID();

    importar("nome,icone\n%s,praca\n\"%s\",estrada\n".formatted(primeira, segunda))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importadas").value(2))
        .andExpect(jsonPath("$.erros").isEmpty());

    assertThat(categoriaService.listar())
        .anyMatch(categoria -> categoria.getNome().equals(primeira))
        .anyMatch(categoria -> categoria.getNome().equals(segunda));
  }

  @Test
  void recusaDuplicidadesDoArquivoSemCriarNada() throws Exception {
    String nome = "Duplicada " + UUID.randomUUID();

    importar("nome,icone\n  %s  ,icone\n%s,icone2\n".formatted(nome, nome.toLowerCase()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importadas").value(0))
        .andExpect(jsonPath("$.erros.length()").value(1))
        .andExpect(jsonPath("$.erros[0].linha").value(3));

    assertThat(categoriaService.listar()).noneMatch(categoria -> categoria.getNome().equals(nome));
  }

  @Test
  void recusaNomeJaCadastradoSemAlterarOCadastro() throws Exception {
    String nome = "Existente " + UUID.randomUUID();
    categoriaService.criar(new CategoriaRegistroDTO(nome, "original"));

    importar("nome,icone\n%s,novo\n".formatted(nome.toLowerCase()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importadas").value(0))
        .andExpect(jsonPath("$.erros[0].linha").value(2));

    assertThat(categoriaService.listar())
        .filteredOn(categoria -> categoria.getNome().equalsIgnoreCase(nome))
        .singleElement()
        .satisfies(categoria -> assertThat(categoria.getIconeNome()).isEqualTo("original"));
  }

  @Test
  void erroDeColunaImpedeImportacaoParcial() throws Exception {
    String nome = "Válida " + UUID.randomUUID();

    importar("nome,icone\n%s,icone\nsem-colunas\n".formatted(nome))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importadas").value(0))
        .andExpect(jsonPath("$.erros[0].linha").value(3));

    assertThat(categoriaService.listar()).noneMatch(categoria -> categoria.getNome().equals(nome));
  }

  @Test
  void arquivoAcimaDoLimiteEhRecusado() throws Exception {
    MockMultipartFile arquivo =
        new MockMultipartFile("arquivo", "categorias.csv", "text/csv", new byte[256 * 1024 + 1]);

    mockMvc
        .perform(
            multipart("/api/v1/gestao/categorias/importacao")
                .file(arquivo)
                .header("Authorization", bearer(tokenGestor)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void cidadaoNaoImportaCategorias() throws Exception {
    String token = cidadaos.token(cidadaos.cadastrar("Maria Souza"));

    mockMvc
        .perform(
            multipart("/api/v1/gestao/categorias/importacao")
                .file(
                    new MockMultipartFile(
                        "arquivo",
                        "categorias.csv",
                        "text/csv",
                        "nome,icone\n".getBytes(StandardCharsets.UTF_8)))
                .header("Authorization", bearer(token)))
        .andExpect(status().isForbidden());
  }

  private ResultActions importar(String conteudo) throws Exception {
    MockMultipartFile arquivo =
        new MockMultipartFile(
            "arquivo", "categorias.csv", "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
    return mockMvc.perform(
        multipart("/api/v1/gestao/categorias/importacao")
            .file(arquivo)
            .header("Authorization", bearer(tokenGestor)));
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class CategoriaUnicidadeIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CategoriaService categoriaService;
  @Autowired private CidadaosDeTeste cidadaos;
  private String tokenGestor;

  @BeforeEach
  void setUp() {
    tokenGestor = cidadaos.tokenDeNovoGestor();
  }

  @Test
  void criacaoRecusaNomeIgnorandoCaixaEEspacos() throws Exception {
    for (String nome : new String[] {"iluminação", "  Iluminação  "}) {
      mockMvc
          .perform(
              post("/api/v1/gestao/categorias")
                  .header("Authorization", "Bearer " + tokenGestor)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"nome\": \"%s\", \"iconeNome\": \"lamp\"}".formatted(nome)))
          .andExpect(status().isConflict());
    }
    assertThat(categoriaService.listar())
        .filteredOn(c -> "Iluminação".equals(c.getNome()))
        .hasSize(1);
  }

  @Test
  void atualizacaoRecusaNomeDeOutraCategoriaSemAlterarAOriginal() throws Exception {
    Integer id = categoriaService.criar(new CategoriaRegistroDTO("Praças", "tree")).getId();

    mockMvc
        .perform(
            put("/api/v1/gestao/categorias/{id}", id)
                .header("Authorization", "Bearer " + tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \" limpeza \", \"iconeNome\": \"tree\"}"))
        .andExpect(status().isConflict());

    assertThat(categoriaService.listar())
        .filteredOn(c -> c.getId().equals(id))
        .singleElement()
        .extracting(c -> c.getNome())
        .isEqualTo("Praças");
  }
}

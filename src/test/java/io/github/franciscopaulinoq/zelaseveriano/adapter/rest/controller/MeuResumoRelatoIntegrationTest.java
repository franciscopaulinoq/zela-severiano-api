package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class MeuResumoRelatoIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void cidadaoSemRelatosRecebeZero() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Ana Lima");

    consultarTotal(cidadaos.token(cidadao), 0);
  }

  @Test
  void totalIncluiTodosOsStatus() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Beto Lima");
    Long emAnalise = registrar(cidadao);
    relatoService.atualizarStatus(emAnalise, StatusRelato.EM_ANALISE, null);
    Long concluido = registrar(cidadao);
    relatoService.atualizarStatus(concluido, StatusRelato.EM_ANALISE, null);
    relatoService.atualizarStatus(concluido, StatusRelato.CONCLUIDO, "Resolvido");
    Long rejeitado = registrar(cidadao);
    relatoService.atualizarStatus(rejeitado, StatusRelato.REJEITADO, "Duplicado");
    relatoService.cancelar(cidadao.usuarioId(), registrar(cidadao));
    registrar(cidadao);

    consultarTotal(cidadaos.token(cidadao), 5);
  }

  @Test
  void relatosDeOutrosCidadaosNaoAlteramOTotal() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Caio Lima");
    Cidadao outro = cidadaos.cadastrar("Dora Lima");
    registrar(cidadao);
    registrar(outro);
    registrar(outro);

    consultarTotal(cidadaos.token(cidadao), 1);
  }

  @Test
  void resumoExigeAutenticacao() throws Exception {
    mockMvc.perform(get("/api/v1/relatos/resumo")).andExpect(status().isForbidden());
  }

  private Long registrar(Cidadao cidadao) {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(1, "Relato de teste", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private void consultarTotal(String token, int total) throws Exception {
    mockMvc
        .perform(get("/api/v1/relatos/resumo").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(total));
  }
}

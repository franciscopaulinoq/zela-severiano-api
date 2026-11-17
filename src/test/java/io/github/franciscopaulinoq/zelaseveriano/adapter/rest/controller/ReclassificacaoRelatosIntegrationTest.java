package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class ReclassificacaoRelatosIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoSpyBean private RelatoRepository relatoRepository;

  private Cidadao cidadao;
  private String tokenGestor;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Autor da Reclassificação");
    tokenGestor = cidadaos.tokenDeNovoGestor();
  }

  @Test
  void loteValidoAtualizaTodosEIgnoraIdentificadoresRepetidos() throws Exception {
    Long primeiro = registrar();
    Long segundo = registrar();
    clearInvocations(relatoRepository);

    mockMvc
        .perform(reclassificar(tokenGestor, corpo(primeiro + "," + segundo + "," + primeiro, 3)))
        .andExpect(status().isNoContent());

    verify(relatoRepository)
        .atualizarCategoriaEmLote(argThat(ids -> ids.size() == 2), any(), any());
    verify(relatoRepository, never()).findAllById(any());
    verify(relatoRepository, never()).findById(any());
    assertThat(categoria(primeiro)).isEqualTo(3);
    assertThat(categoria(segundo)).isEqualTo(3);
  }

  @Test
  void identificadorInexistenteEhInformadoNaResposta() throws Exception {
    Long existente = registrar();
    Long inexistente = 999999L;

    mockMvc
        .perform(reclassificar(tokenGestor, corpo(existente + "," + inexistente, 3)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Relato não encontrado: " + inexistente));
  }

  @Test
  void categoriaInexistenteRecusaAntesDeAlterarRelatos() throws Exception {
    Long relatoId = registrar();
    clearInvocations(relatoRepository);

    mockMvc
        .perform(reclassificar(tokenGestor, corpo(String.valueOf(relatoId), 999)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Categoria não encontrada."));

    verify(relatoRepository, never()).findAllById(any());
    verify(relatoRepository, never()).atualizarCategoriaEmLote(any(), any(), any());
    assertThat(categoria(relatoId)).isEqualTo(1);
  }

  @Test
  void cidadaoComumNaoPodeReclassificar() throws Exception {
    String tokenCidadao = cidadaos.token(cidadao);
    Long relatoId = registrar();
    clearInvocations(relatoRepository);

    mockMvc
        .perform(reclassificar(tokenCidadao, corpo(String.valueOf(relatoId), 3)))
        .andExpect(status().isForbidden());

    verifyNoInteractions(relatoRepository);
  }

  @Test
  void contratoRecusaLoteVazioOuAcimaDoLimite() throws Exception {
    String muitosIds =
        LongStream.rangeClosed(1, 101).mapToObj(String::valueOf).collect(Collectors.joining(","));

    mockMvc.perform(reclassificar(tokenGestor, corpo("", 3))).andExpect(status().isBadRequest());
    mockMvc
        .perform(reclassificar(tokenGestor, corpo(muitosIds, 3)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(relatoRepository);
  }

  @Test
  void contratoRecusaIdentificadoresNulosOuNaoPositivos() throws Exception {
    for (String ids : new String[] {"null", "0", "-1"}) {
      mockMvc.perform(reclassificar(tokenGestor, corpo(ids, 3))).andExpect(status().isBadRequest());
    }
    mockMvc
        .perform(reclassificar(tokenGestor, corpo("1", null)))
        .andExpect(status().isBadRequest());
    mockMvc.perform(reclassificar(tokenGestor, corpo("1", 0))).andExpect(status().isBadRequest());
    verifyNoInteractions(relatoRepository);
  }

  private Long registrar() {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(1, "Relato para reclassificar", -6.08, -38.38),
            null,
            null,
            null)
        .getId();
  }

  private Integer categoria(Long relatoId) {
    return jdbcTemplate.queryForObject(
        "SELECT categoria_id FROM relatos WHERE id = ?", Integer.class, relatoId);
  }

  private static String corpo(String ids, Integer categoriaId) {
    return "{\"relatoIds\": [%s], \"categoriaId\": %s}".formatted(ids, categoriaId);
  }

  private static MockHttpServletRequestBuilder reclassificar(String token, String corpo) {
    return patch("/api/v1/gestao/relatos/reclassificacao")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(corpo);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GaleriaGestaoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoBean private FileStorage fileStorage;

  private Cidadao cidadao;
  private String tokenGestor;
  private Long relatoId;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Autor da galeria");
    tokenGestor = cidadaos.tokenDeNovoGestor();
    when(fileStorage.upload(any(), anyString(), anyString()))
        .thenReturn(
            "https://fotos.test/original.jpg",
            "https://fotos.test/complementar-1.jpg",
            "https://fotos.test/complementar-2.jpg");
    relatoId =
        relatoService
            .registrar(
                cidadao.usuarioId(),
                new RelatoRegistroDTO(1, "Relato com galeria", -6.08, -38.38),
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "original.jpg",
                "image/jpeg")
            .getId();
  }

  @Test
  void gestorAdicionaEConsultaItensEmOrdemCronologica() throws Exception {
    adicionarFoto().andExpect(status().isCreated()).andExpect(jsonPath("$.autorId").isNotEmpty());
    adicionarFoto().andExpect(status().isCreated());
    adicionarComentario("Primeiro comentário").andExpect(status().isCreated());
    adicionarComentario("  Segundo comentário  ")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.texto").value("Segundo comentário"));

    detalhe(tokenGestor)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.urlFoto").value("https://fotos.test/original.jpg"))
        .andExpect(jsonPath("$.fotos.length()").value(2))
        .andExpect(
            jsonPath("$.fotos[*].urlFoto")
                .value(
                    contains(
                        "https://fotos.test/complementar-1.jpg",
                        "https://fotos.test/complementar-2.jpg")))
        .andExpect(jsonPath("$.fotos[*].autorId").isNotEmpty())
        .andExpect(jsonPath("$.fotos[*].criadoEm").isNotEmpty())
        .andExpect(
            jsonPath("$.comentarios[*].texto")
                .value(contains("Primeiro comentário", "Segundo comentário")));
  }

  @Test
  void detalheSemItensRetornaColecoesVazias() throws Exception {
    detalhe(tokenGestor)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fotos.length()").value(0))
        .andExpect(jsonPath("$.comentarios.length()").value(0));
  }

  @Test
  void comentarioVazioEhRecusado() throws Exception {
    adicionarComentario("   ").andExpect(status().isBadRequest());
  }

  @Test
  void cidadaoNaoConsultaNemAdicionaItensDeGestao() throws Exception {
    String tokenCidadao = cidadaos.token(cidadao);

    detalhe(tokenCidadao).andExpect(status().isForbidden());
    adicionarFoto(tokenCidadao).andExpect(status().isForbidden());
    adicionarComentario("Sem permissão", tokenCidadao).andExpect(status().isForbidden());
  }

  private org.springframework.test.web.servlet.ResultActions detalhe(String token)
      throws Exception {
    return mockMvc.perform(
        get("/api/v1/gestao/relatos/{id}/detalhe-ampliado", relatoId)
            .header("Authorization", "Bearer " + token));
  }

  private org.springframework.test.web.servlet.ResultActions adicionarFoto() throws Exception {
    return adicionarFoto(tokenGestor);
  }

  private org.springframework.test.web.servlet.ResultActions adicionarFoto(String token)
      throws Exception {
    return mockMvc.perform(
        multipart("/api/v1/gestao/relatos/{id}/fotos", relatoId)
            .file(
                new MockMultipartFile(
                    "foto",
                    "complementar.jpg",
                    "image/jpeg",
                    new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}))
            .header("Authorization", "Bearer " + token));
  }

  private org.springframework.test.web.servlet.ResultActions adicionarComentario(String texto)
      throws Exception {
    return adicionarComentario(texto, tokenGestor);
  }

  private org.springframework.test.web.servlet.ResultActions adicionarComentario(
      String texto, String token) throws Exception {
    return mockMvc.perform(
        post("/api/v1/gestao/relatos/{id}/comentarios", relatoId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"texto\":\"%s\"}".formatted(texto)));
  }
}

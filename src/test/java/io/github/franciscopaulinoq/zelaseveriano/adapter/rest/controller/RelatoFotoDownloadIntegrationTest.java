package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.ArmazenamentoIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.ArquivoArmazenado;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoFotoDownloadIntegrationTest {
  private static final String URL_FOTO = "https://arquivos.example.com/foto.jpg";
  private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};

  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private RelatoService relatoService;
  @MockitoBean private FileStorage fileStorage;

  private Cidadao cidadao;
  private String token;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Beatriz Rocha");
    token = cidadaos.token(cidadao);
    when(fileStorage.upload(any(), anyString(), anyString())).thenReturn(URL_FOTO);
  }

  @Test
  void downloadDevolveBytesETipoDaImagem() throws Exception {
    Long relatoId = registrar(true);
    when(fileStorage.baixar(URL_FOTO)).thenReturn(new ArquivoArmazenado(JPEG, "image/jpeg"));

    mockMvc
        .perform(download(relatoId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(JPEG));
  }

  @Test
  void relatoSemFotoDevolve404SemChamarArmazenamento() throws Exception {
    Long relatoId = registrar(false);

    mockMvc.perform(download(relatoId)).andExpect(status().isNotFound());

    verify(fileStorage, never()).baixar(anyString());
  }

  @Test
  void relatoInexistenteDevolve404SemChamarArmazenamento() throws Exception {
    mockMvc.perform(download(999999L)).andExpect(status().isNotFound());

    verify(fileStorage, never()).baixar(anyString());
  }

  @Test
  void falhaDoArmazenamentoDevolve503() throws Exception {
    Long relatoId = registrar(true);
    when(fileStorage.baixar(URL_FOTO))
        .thenThrow(
            new ArmazenamentoIndisponivelException(
                "Falha externa", new IOException("indisponível")));

    mockMvc
        .perform(download(relatoId))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  private Long registrar(boolean comFoto) {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(1, "Poste apagado", -6.08, -38.38),
            comFoto ? JPEG : null,
            comFoto ? "poste.jpg" : null,
            comFoto ? "image/jpeg" : null)
        .getId();
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder download(
      Long relatoId) {
    return get("/api/v1/relatos/{id}/foto", relatoId).header("Authorization", "Bearer " + token);
  }
}

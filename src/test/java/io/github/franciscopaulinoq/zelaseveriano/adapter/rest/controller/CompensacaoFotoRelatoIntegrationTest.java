package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class CompensacaoFotoRelatoIntegrationTest {
  private static final String URL_FOTO = "https://arquivos.example.com/poste.jpg";

  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoBean private FileStorage fileStorage;
  @MockitoSpyBean private RelatoRepository relatoRepository;
  private String token;

  @BeforeEach
  void setUp() {
    token = cidadaos.token(cidadaos.cadastrar("Usuário de Upload"));
  }

  @Test
  void falhaDaPersistenciaAposMultipartSolicitaExclusaoDoObjeto() throws Exception {
    when(fileStorage.upload(any(), endsWith(".jpg"), eq("image/jpeg"))).thenReturn(URL_FOTO);
    doThrow(new IllegalStateException("Falha ao salvar")).when(relatoRepository).save(any());

    mockMvc
        .perform(
            multipart("/api/v1/relatos")
                .file(dados(-6.08))
                .file(foto())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isInternalServerError());

    verify(fileStorage).excluir(URL_FOTO);
  }

  @Test
  void recusaAntesDoUploadNaoSolicitaExclusao() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/relatos")
                .file(dados(-5.79))
                .file(foto())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isUnprocessableContent());

    verifyNoInteractions(fileStorage);
    verify(relatoRepository, never()).save(any());
  }

  private static MockMultipartFile foto() {
    return new MockMultipartFile(
        "foto", "poste.jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0});
  }

  private static MockMultipartFile dados(double latitude) {
    String json =
        """
        {"categoriaId": 1, "descricao": "Poste apagado", "latitude": %s, "longitude": -38.38}
        """
            .formatted(latitude);
    return new MockMultipartFile(
        "dados", "dados.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
  }
}

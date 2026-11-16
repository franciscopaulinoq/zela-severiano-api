package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class UploadRelatoIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CidadaosDeTeste cidadaos;

  private String token;

  @BeforeEach
  void setUp() {
    Cidadao cidadao = cidadaos.cadastrar("Usuário de Upload");
    token = cidadaos.token(cidadao);
  }

  @Test
  void fotoAcimaDoLimiteEhRecusadaPeloServico() throws Exception {
    MockMultipartFile dados = dados();
    MockMultipartFile foto =
        new MockMultipartFile("foto", "grande.jpg", "image/jpeg", new byte[10 * 1024 * 1024 + 1]);

    mockMvc
        .perform(
            multipart("/api/v1/relatos")
                .file(dados)
                .file(foto)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.message").value("A foto deve ter no máximo 10 MB."));
  }

  @Test
  void multipartSemDadosVira400() throws Exception {
    MockMultipartFile foto =
        new MockMultipartFile(
            "foto", "poste.jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

    mockMvc
        .perform(multipart("/api/v1/relatos").file(foto).header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Requisição inválida"));
  }

  private static MockMultipartFile dados() {
    String json =
        """
        {"categoriaId": 1, "descricao": "Poste apagado", "latitude": -6.08, "longitude": -38.38}
        """;
    return new MockMultipartFile(
        "dados", "dados.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.service.PerfilService;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.IntegracaoCepIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.CepGateway;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class ConsultaCepIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private PerfilService perfilService;
  @MockitoBean private CepGateway cepGateway;

  private Cidadao cidadao;
  private String token;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Consulta de CEP");
    token = cidadaos.token(cidadao);
  }

  @Test
  void cepComMascaraRetornaEnderecoSemSalvarNoPerfil() throws Exception {
    var perfilAntes = perfilService.obterMeuPerfil(cidadao.usuarioId());
    when(cepGateway.consultar("59680000"))
        .thenReturn(
            Optional.of(new EnderecoCep("Rua Principal", "Centro", "Doutor Severiano", "RN")));

    consultar("59680-000")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.logradouro").value("Rua Principal"))
        .andExpect(jsonPath("$.bairro").value("Centro"))
        .andExpect(jsonPath("$.cidade").value("Doutor Severiano"))
        .andExpect(jsonPath("$.uf").value("RN"));

    assertThat(perfilService.obterMeuPerfil(cidadao.usuarioId()))
        .usingRecursiveComparison()
        .isEqualTo(perfilAntes);
    verify(cepGateway).consultar("59680000");
  }

  @Test
  void cepInexistenteInformaPreenchimentoManual() throws Exception {
    when(cepGateway.consultar("59680001")).thenReturn(Optional.empty());

    consultar("59680-001")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("manual")));
  }

  @Test
  void indisponibilidadeExternaPermiteNovaTentativa() throws Exception {
    when(cepGateway.consultar("59680002"))
        .thenThrow(new IntegracaoCepIndisponivelException(new IOException("falha simulada")));

    consultar("59680-002")
        .andExpect(status().isServiceUnavailable())
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Tente novamente")));
  }

  @Test
  void formatoInvalidoNaoChamaIntegracao() throws Exception {
    consultar("5968-000")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.validation_errors.cep").exists());
    verifyNoInteractions(cepGateway);
  }

  @Test
  void consultaExigeAutenticacao() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/enderecos/cep")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cep\":\"59680000\"}"))
        .andExpect(status().isForbidden());
    verifyNoInteractions(cepGateway);
  }

  private org.springframework.test.web.servlet.ResultActions consultar(String cep)
      throws Exception {
    return mockMvc.perform(
        post("/api/v1/enderecos/cep")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cep\":\"%s\"}".formatted(cep)));
  }
}

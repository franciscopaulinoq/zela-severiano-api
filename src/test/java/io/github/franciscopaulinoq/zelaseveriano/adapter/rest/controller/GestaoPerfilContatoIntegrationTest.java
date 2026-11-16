package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RegistrarCidadaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.CpfsDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoPerfilContatoIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private ContaService contaService;

  private String tokenGestor;

  @BeforeEach
  void setUp() {
    tokenGestor = cidadaos.tokenDeNovoGestor();
  }

  @Test
  void gestorConsultaNomeEEmailDoCidadao() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Helena Costa");

    mockMvc
        .perform(
            get("/api/v1/gestao/perfis/{id}/contato", cidadao.perfilId())
                .header("Authorization", "Bearer " + tokenGestor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(cidadao.perfilId().toString()))
        .andExpect(jsonPath("$.nomeCompleto").value("Helena Costa"))
        .andExpect(jsonPath("$.email").value("cidadao@example.com"));
  }

  @Test
  void contatoSemEmailRetornaEmailNaoInformado() throws Exception {
    UUID perfilId =
        contaService
            .registrarCidadao(
                RegistrarCidadaoDTO.builder()
                    .cpf(CpfsDeTeste.novo())
                    .nomeCompleto("João Sem Email")
                    .logradouro("Rua Principal")
                    .numero("10")
                    .bairro("Centro")
                    .build(),
                CidadaosDeTeste.SENHA)
            .perfilId();

    mockMvc
        .perform(
            get("/api/v1/gestao/perfis/{id}/contato", perfilId)
                .header("Authorization", "Bearer " + tokenGestor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nomeCompleto").value("João Sem Email"))
        .andExpect(jsonPath("$.email").value(nullValue()));
  }

  @Test
  void perfilInexistenteRetornaNaoEncontrado() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/perfis/{id}/contato", UUID.randomUUID())
                .header("Authorization", "Bearer " + tokenGestor))
        .andExpect(status().isNotFound());
  }

  @Test
  void cidadaoNaoConsultaContatosPelaGestao() throws Exception {
    Cidadao cidadao = cidadaos.cadastrar("Otávio Reis");

    mockMvc
        .perform(
            get("/api/v1/gestao/perfis/{id}/contato", cidadao.perfilId())
                .header("Authorization", "Bearer " + cidadaos.token(cidadao)))
        .andExpect(status().isForbidden());
  }
}

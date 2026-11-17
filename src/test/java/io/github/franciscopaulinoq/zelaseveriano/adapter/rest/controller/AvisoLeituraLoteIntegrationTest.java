package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.AvisoPublicacaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.AvisoService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class AvisoLeituraLoteIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private AvisoService avisoService;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void publicacaoAlcancaSomenteOsDestinatarios() throws Exception {
    Cidadao primeiro = cidadaos.cadastrar("Ana Souza");
    Cidadao segundo = cidadaos.cadastrar("Bruno Lima");
    Cidadao terceiro = cidadaos.cadastrar("Carlos Reis");
    String corpo =
        """
        {"titulo":"Interdição","conteudo":"Obra na avenida","destinatarios":["%s","%s"]}
        """
            .formatted(primeiro.perfilId(), segundo.perfilId());
    String gestor = cidadaos.tokenDeNovoGestor();

    mockMvc
        .perform(
            post("/api/v1/gestao/avisos")
                .header("Authorization", "Bearer " + gestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
        .andExpect(status().isCreated());
    caixa(primeiro)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].lidaEm").value(nullValue()));
    caixa(segundo).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    caixa(terceiro).andExpect(jsonPath("$", hasSize(0)));
    mockMvc
        .perform(post("/api/v1/gestao/avisos").header("Authorization", bearer(primeiro)))
        .andExpect(status().isForbidden());
  }

  @Test
  void lotePreservaLeituraAnteriorEIgualaNovasLeituras() throws Exception {
    Cidadao autor = cidadaos.cadastrar("Ana Souza");
    Cidadao outro = cidadaos.cadastrar("Bruno Lima");
    Long primeiro = publicar("Aviso 1", autor);
    Long segundo = publicar("Aviso 2", autor);
    Long terceiro = publicar("Aviso 3", autor);
    Long alheio = publicar("Aviso alheio", outro);
    avisoService.marcarComoLidos(autor.usuarioId(), List.of(primeiro));
    OffsetDateTime leituraOriginal = obterAviso(autor, primeiro).lidaEm();

    mockMvc
        .perform(
            patch("/api/v1/avisos/leitura")
                .header("Authorization", bearer(autor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"avisoIds\":[%d,%d,%d,%d,%d]}"
                        .formatted(primeiro, segundo, terceiro, alheio, segundo)))
        .andExpect(status().isNoContent());

    assertThat(obterAviso(autor, primeiro).lidaEm()).isEqualTo(leituraOriginal);
    assertThat(obterAviso(autor, segundo).lidaEm())
        .isNotNull()
        .isEqualTo(obterAviso(autor, terceiro).lidaEm());
    assertThat(obterAviso(outro, alheio).lidaEm()).isNull();
  }

  private ResultActions caixa(Cidadao cidadao) throws Exception {
    return mockMvc.perform(get("/api/v1/avisos").header("Authorization", bearer(cidadao)));
  }

  private Long publicar(String titulo, Cidadao... destinatarios) {
    return avisoService
        .publicar(
            new AvisoPublicacaoDTO(
                titulo, "Conteúdo", Arrays.stream(destinatarios).map(Cidadao::perfilId).toList()))
        .id();
  }

  private AvisoDTO obterAviso(Cidadao cidadao, Long avisoId) {
    return avisoService.listarMeus(cidadao.usuarioId()).stream()
        .filter(aviso -> aviso.id().equals(avisoId))
        .findFirst()
        .orElseThrow();
  }

  private String bearer(Cidadao cidadao) {
    return "Bearer " + cidadaos.token(cidadao);
  }
}

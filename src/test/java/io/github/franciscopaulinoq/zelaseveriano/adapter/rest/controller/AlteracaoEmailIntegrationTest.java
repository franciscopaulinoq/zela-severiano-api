package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.domain.notification.ConfirmacaoEmailGateway;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class AlteracaoEmailIntegrationTest {
  private static final Instant INICIO = Instant.parse("2026-01-10T12:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoBean private ConfirmacaoEmailGateway emailGateway;
  @MockitoBean private Clock clock;

  private String token;

  @BeforeEach
  void setUp() {
    when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);
    when(clock.instant()).thenReturn(INICIO);
    token = cidadaos.token(cidadaos.cadastrar("Helena Costa"));
  }

  @Test
  void solicitacaoPreservaEmailECodigoCorretoConfirmaUmaUnicaVez() throws Exception {
    String email = "novo@example.com";
    solicitar(email)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("cidadao@example.com"));
    String codigo = codigoEnviado(email);
    org.assertj.core.api.Assertions.assertThat(codigo).matches("\\d{6}");

    confirmar(codigo).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
    confirmar(codigo).andExpect(status().isUnprocessableContent());

    mockMvc
        .perform(get("/api/v1/perfis/me").header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  void codigoIncorretoOuExpiradoNaoAlteraEmail() throws Exception {
    String email = "novo@example.com";
    solicitar(email).andExpect(status().isOk());
    String codigo = codigoEnviado(email);
    String incorreto = "%06d".formatted((Integer.parseInt(codigo) + 1) % 1_000_000);

    confirmar(incorreto).andExpect(status().isUnprocessableContent());
    when(clock.instant()).thenReturn(INICIO.plus(16, ChronoUnit.MINUTES));
    confirmar(codigo).andExpect(status().isUnprocessableContent());

    mockMvc
        .perform(get("/api/v1/perfis/me").header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.email").value("cidadao@example.com"));
  }

  private org.springframework.test.web.servlet.ResultActions solicitar(String email)
      throws Exception {
    return mockMvc.perform(
        patch("/api/v1/perfis/me")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\"}".formatted(email)));
  }

  private org.springframework.test.web.servlet.ResultActions confirmar(String codigo)
      throws Exception {
    return mockMvc.perform(
        post("/api/v1/perfis/me/email/confirmacao")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"codigo\":\"%s\"}".formatted(codigo)));
  }

  private String codigoEnviado(String email) {
    ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
    verify(emailGateway).enviar(eq(email), codigo.capture());
    return codigo.getValue();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.SolicitarRecuperacaoSenhaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.ContaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RecuperacaoSenhaService;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CredenciaisInvalidasException;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.RecuperacaoSenhaEmailGateway;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.CpfsDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RecuperacaoSenhaIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ContaService contaService;
  @Autowired private CidadaosDeTeste cidadaos;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private RecuperacaoSenhaService recuperacaoService;
  @Autowired private PlatformTransactionManager transactionManager;
  @MockitoBean private RecuperacaoSenhaEmailGateway emailGateway;

  private Cidadao cidadao;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Marcos Dantas");
  }

  @Test
  void solicitacaoTemMesmaRespostaParaCpfCadastradoEInexistente() throws Exception {
    String conhecido = corpoSolicitacao(cidadao.cpf());
    ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
    verify(emailGateway).enviar(eq("cidadao@example.com"), codigo.capture());
    assertThat(conhecido).doesNotContain(codigo.getValue());

    String inexistente = corpoSolicitacao(CpfsDeTeste.novo());

    assertThat(inexistente).isEqualTo(conhecido);
    verifyNoMoreInteractions(emailGateway);
  }

  @Test
  void rollbackDaSolicitacaoNaoEnviaCodigo() {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(transaction -> {
          recuperacaoService.solicitar(new SolicitarRecuperacaoSenhaDTO(cidadao.cpf()));
          transaction.setRollbackOnly();
        });

    verifyNoInteractions(emailGateway);
  }

  @Test
  void codigoValidoAlteraSenhaENaoPodeSerReutilizado() throws Exception {
    String codigo = solicitarCodigo();

    confirmar(codigo, "nova-senha-123").andExpect(status().isNoContent());

    assertThat(contaService.autenticar(cidadao.cpf(), "nova-senha-123").tokenAcesso()).isNotBlank();
    confirmar(codigo, "outra-senha-123").andExpect(status().isUnprocessableContent());
    assertThatThrownBy(() -> contaService.autenticar(cidadao.cpf(), CidadaosDeTeste.SENHA))
        .isInstanceOf(CredenciaisInvalidasException.class);
  }

  @Test
  void codigoIncorretoNaoAlteraSenha() throws Exception {
    String codigo = solicitarCodigo();
    String incorreto = "000000".equals(codigo) ? "000001" : "000000";

    confirmar(incorreto, "nova-senha-123").andExpect(status().isUnprocessableContent());

    assertThat(contaService.autenticar(cidadao.cpf(), CidadaosDeTeste.SENHA).tokenAcesso())
        .isNotBlank();
  }

  @Test
  void codigoExpiradoNaoAlteraSenha() throws Exception {
    String codigo = solicitarCodigo();
    jdbcTemplate.update(
        "update recuperacoes_senha set expira_em = ? where usuario_id = ?",
        OffsetDateTime.now().minusSeconds(1),
        cidadao.usuarioId());

    confirmar(codigo, "nova-senha-123").andExpect(status().isUnprocessableContent());

    assertThat(contaService.autenticar(cidadao.cpf(), CidadaosDeTeste.SENHA).tokenAcesso())
        .isNotBlank();
  }

  @Test
  void novaSolicitacaoInvalidaCodigoAnterior() throws Exception {
    String anterior = solicitarCodigo();
    String atual = solicitarCodigo();
    while (atual.equals(anterior)) {
      anterior = atual;
      atual = solicitarCodigo();
    }

    confirmar(anterior, "senha-antiga-123").andExpect(status().isUnprocessableContent());
    confirmar(atual, "nova-senha-123").andExpect(status().isNoContent());

    assertThat(contaService.autenticar(cidadao.cpf(), "nova-senha-123").tokenAcesso()).isNotBlank();
  }

  private String solicitarCodigo() throws Exception {
    solicitar(cidadao.cpf()).andExpect(status().isAccepted());
    ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
    verify(emailGateway).enviar(eq("cidadao@example.com"), codigo.capture());
    clearInvocations(emailGateway);
    return codigo.getValue();
  }

  private String corpoSolicitacao(String cpf) throws Exception {
    return solicitar(cpf)
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.mensagem").exists())
        .andExpect(jsonPath("$.codigo").doesNotExist())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private ResultActions solicitar(String cpf) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/recuperacao/solicitar")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cpf\":\"%s\"}".formatted(cpf)));
  }

  private ResultActions confirmar(String codigo, String novaSenha) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/recuperacao/confirmar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"cpf\":\"%s\",\"codigo\":\"%s\",\"novaSenha\":\"%s\"}"
                    .formatted(cidadao.cpf(), codigo, novaSenha)));
  }
}

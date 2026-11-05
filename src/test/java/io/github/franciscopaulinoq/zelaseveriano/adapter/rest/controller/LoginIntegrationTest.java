package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenPayload;
import io.github.franciscopaulinoq.zelaseveriano.domain.security.TokenService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.CpfsDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CidadaosDeTeste cidadaos;

    @Autowired
    private TokenService tokenService;

    private Cidadao cidadao;

    @BeforeEach
    void setUp() {
        cidadao = cidadaos.cadastrar("Beatriz Rocha");
    }

    @Test
    void credenciaisCorretasDevolvemTokenDeAcessoDoProprioCidadao() throws Exception {
        MvcResult resultado = login(cidadao.cpf(), CidadaosDeTeste.SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String token = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
        Optional<TokenPayload> payload = tokenService.validarToken(token);

        assertThat(payload).hasValueSatisfying(dados -> {
            assertThat(dados.usuarioId()).isEqualTo(cidadao.usuarioId());
            assertThat(dados.role()).isEqualTo(Role.CIDADAO);
        });
    }

    @Test
    void senhaIncorretaEhRecusadaSemToken() throws Exception {
        login(cidadao.cpf(), "senha-errada")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.message").value("CPF ou senha inválidos"));
    }

    @Test
    void cpfNaoCadastradoTemAMesmaRespostaDaSenhaIncorreta() throws Exception {
        String senhaErrada =
                login(cidadao.cpf(), "senha-errada").andReturn().getResponse().getContentAsString();
        String cpfInexistente = login(CpfsDeTeste.novo(), CidadaosDeTeste.SENHA)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(JsonPath.<String>read(cpfInexistente, "$.message"))
                .isEqualTo(JsonPath.<String>read(senhaErrada, "$.message"));
        assertThat(JsonPath.<Integer>read(cpfInexistente, "$.status"))
                .isEqualTo(JsonPath.<Integer>read(senhaErrada, "$.status"));
    }

    @Test
    void camposAusentesSaoRecusadosNaValidacao() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.cpf").exists())
                .andExpect(jsonPath("$.validation_errors.senha").exists());
    }

    private ResultActions login(String cpf, String senha) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"cpf": "%s", "senha": "%s"}
                        """.formatted(cpf, senha)));
    }
}

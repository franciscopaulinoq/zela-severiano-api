package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.UsuarioRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.security.JwtService;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RenovacaoSessaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CidadaosDeTeste cidadaos;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${api.security.token.secret}")
    private String secret;

    @Test
    void tokenDeRenovacaoDoLoginEmiteNovoTokenDeAcessoValido() throws Exception {
        Cidadao cidadao = cidadaos.cadastrar("Tiago Nunes");
        String corpoLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "%s", "senha": "%s"}
                                """.formatted(cidadao.cpf(), CidadaosDeTeste.SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String refreshToken = JsonPath.read(corpoLogin, "$.refreshToken");

        String corpoRenovacao = renovar(refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String novoToken = JsonPath.read(corpoRenovacao, "$.token");

        mockMvc.perform(get("/api/v1/perfis/me").header("Authorization", "Bearer " + novoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Tiago Nunes"));
    }

    @Test
    void tokenDeRenovacaoExpiradoEhRecusadoSemEmitirToken() throws Exception {
        Cidadao cidadao = cidadaos.cadastrar("Vera Lúcia");
        JwtService geradoHaOitoDias =
                new JwtService(Clock.fixed(Instant.now().minus(Duration.ofDays(8)), ZoneOffset.UTC));
        ReflectionTestUtils.setField(geradoHaOitoDias, "secret", secret);
        String expirado = geradoHaOitoDias.gerarTokenDeRenovacao(
                usuarioRepository.findById(cidadao.usuarioId()).orElseThrow());

        renovar(expirado)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    private ResultActions renovar(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken)));
    }
}

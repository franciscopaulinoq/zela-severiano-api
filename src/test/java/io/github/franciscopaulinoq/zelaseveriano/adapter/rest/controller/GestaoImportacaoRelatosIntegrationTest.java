package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoImportacaoRelatosIntegrationTest {

  private static final String URL = "/api/v1/gestao/relatos/importacao";
  private static final String CABECALHO =
      "identificador_externo,cpf,categoria_id,descricao,latitude,longitude,status,criado_em\n";
  private static final String DATA = "2020-01-02T03:04:05-03:00";

  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoRepository relatoRepository;
  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void importaPreservandoAutoresCategoriasStatusEDatas() throws Exception {
    Cidadao primeiroAutor = cidadaos.cadastrar("Igor Nunes");
    Cidadao segundoAutor = cidadaos.cadastrar("Joana Nunes");
    String csv =
        CABECALHO
            + linha("rest-valido-1", primeiroAutor.cpf(), 1, "CONCLUIDO", DATA)
            + linha(
                "rest-valido-2", segundoAutor.cpf(), 2, "EM_ANALISE", "2021-02-03T04:05:06-03:00");

    importar(cidadaos.tokenDeNovoGestor(), csv)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.totalImportado").value(2));

    Relato primeiro = relatoRepository.findByIdentificadorExterno("rest-valido-1").orElseThrow();
    Relato segundo = relatoRepository.findByIdentificadorExterno("rest-valido-2").orElseThrow();
    assertThat(primeiro.getPerfil().getId()).isEqualTo(primeiroAutor.perfilId());
    assertThat(primeiro.getCategoria().getId()).isEqualTo(1);
    assertThat(primeiro.getStatus()).isEqualTo(StatusRelato.CONCLUIDO);
    assertThat(primeiro.getCriadoEm().toInstant())
        .isEqualTo(OffsetDateTime.parse(DATA).toInstant());
    assertThat(segundo.getPerfil().getId()).isEqualTo(segundoAutor.perfilId());
    assertThat(segundo.getCategoria().getId()).isEqualTo(2);
    assertThat(segundo.getStatus()).isEqualTo(StatusRelato.EM_ANALISE);
  }

  @Test
  void informaErrosPorLinhaENaoSalvaRegistroValido() throws Exception {
    Cidadao autor = cidadaos.cadastrar("Lara Nunes");
    String csv =
        CABECALHO
            + linha("rest-erros-valido", autor.cpf(), 1, "PENDENTE", DATA)
            + linha("rest-sem-perfil", "11144477735", 1, "PENDENTE", DATA)
            + linha("rest-sem-categoria", autor.cpf(), 999, "PENDENTE", DATA)
            + "rest-fora,"
            + autor.cpf()
            + ",1,Relato,0,0,PENDENTE,"
            + DATA
            + "\n"
            + linha("rest-status", autor.cpf(), 1, "INEXISTENTE", DATA);

    importar(cidadaos.tokenDeNovoGestor(), csv)
        .andExpect(status().isUnprocessableContent())
        .andExpect(
            jsonPath(
                "$.message",
                allOf(
                    containsString("Linha 3"),
                    containsString("CPF sem perfil"),
                    containsString("Linha 4"),
                    containsString("categoria inexistente"),
                    containsString("Linha 5"),
                    containsString("fora do município"),
                    containsString("Linha 6"),
                    containsString("status desconhecido"))));
    assertThat(relatoRepository.findByIdentificadorExterno("rest-erros-valido")).isEmpty();
  }

  @Test
  void desfazLoteValidoQuandoErroOcorreNoFinalDoArquivo() throws Exception {
    Cidadao autor = cidadaos.cadastrar("Mauro Nunes");
    StringBuilder csv = new StringBuilder(CABECALHO);
    for (int i = 0; i < 51; i++) {
      csv.append(linha("rest-atomico-" + i, autor.cpf(), 1, "CONCLUIDO", DATA));
    }
    csv.append(linha("rest-atomico-invalido", autor.cpf(), 999, "PENDENTE", DATA));

    importar(cidadaos.tokenDeNovoGestor(), csv.toString())
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.message", containsString("Linha 53")));

    assertThat(relatoRepository.findByIdentificadorExterno("rest-atomico-0")).isEmpty();
  }

  @Test
  void reimportacaoComMesmoIdentificadorNaoCriaOutroRelato() throws Exception {
    Cidadao autor = cidadaos.cadastrar("Nina Nunes");
    String gestor = cidadaos.tokenDeNovoGestor();
    String csv = CABECALHO + linha("rest-duplicado", autor.cpf(), 1, "PENDENTE", DATA);
    importar(gestor, csv).andExpect(status().isCreated());
    long total = relatoRepository.count();

    importar(gestor, csv)
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.message", containsString("identificador externo duplicado")));

    assertThat(relatoRepository.count()).isEqualTo(total);
  }

  @Test
  void cidadaoComumNaoPodeImportar() throws Exception {
    Cidadao autor = cidadaos.cadastrar("Otávio Nunes");

    importar(cidadaos.token(autor), CABECALHO).andExpect(status().isForbidden());
  }

  private ResultActions importar(String token, String csv) throws Exception {
    MockMultipartFile arquivo =
        new MockMultipartFile(
            "arquivo", "relatos.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
    return mockMvc.perform(multipart(URL).file(arquivo).header("Authorization", "Bearer " + token));
  }

  private static String linha(
      String id, String cpf, int categoriaId, String status, String criadoEm) {
    return "%s,%s,%d,Relato legado,-6.08,-38.38,%s,%s\n"
        .formatted(id, cpf, categoriaId, status, criadoEm);
  }
}

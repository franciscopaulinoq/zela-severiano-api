package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.controller;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.service.CategoriaService;
import io.github.franciscopaulinoq.zelaseveriano.application.service.RelatoService;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class GestaoMidiaIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private RelatoService relatoService;
  @Autowired private CategoriaService categoriaService;
  @Autowired private CidadaosDeTeste cidadaos;
  @MockitoBean private FileStorage fileStorage;

  private Cidadao cidadao;
  private String tokenGestor;
  private Integer categoriaFotos;
  private Integer categoriaSemFoto;
  private Long fotoPresente;
  private Long fotoAusente;
  private Long semFoto;

  @BeforeEach
  void setUp() {
    cidadao = cidadaos.cadastrar("Autor das fotos");
    tokenGestor = cidadaos.tokenDeNovoGestor();
    categoriaFotos = criarCategoria("Fotos");
    categoriaSemFoto = criarCategoria("Sem foto");
    when(fileStorage.upload(any(), anyString(), anyString()))
        .thenReturn("https://fotos.test/presente.jpg", "https://fotos.test/ausente.jpg");
    fotoPresente = registrar(categoriaFotos, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    fotoAusente = registrar(categoriaFotos, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    semFoto = registrar(categoriaSemFoto, null);
  }

  @Test
  void gestorVeFotosDisponiveisEIndisponiveis() throws Exception {
    when(fileStorage.existe("https://fotos.test/presente.jpg")).thenReturn(true);
    when(fileStorage.existe("https://fotos.test/ausente.jpg")).thenReturn(false);

    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/disponibilidade-fotos")
                .param("categoriaId", String.valueOf(categoriaFotos))
                .header("Authorization", "Bearer " + tokenGestor))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[?(@.relatoId == %d)].situacao", fotoPresente)
                .value(contains("DISPONIVEL")))
        .andExpect(
            jsonPath("$.content[?(@.relatoId == %d)].situacao", fotoAusente)
                .value(contains("INDISPONIVEL")))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(fileStorage, times(2)).existe(anyString());
    verify(fileStorage).existe("https://fotos.test/presente.jpg");
    verify(fileStorage).existe("https://fotos.test/ausente.jpg");
  }

  @Test
  void filtrosEPaginacaoDefinemOsItensVerificados() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/disponibilidade-fotos")
                .param("categoriaId", String.valueOf(categoriaSemFoto))
                .param("pagina", "0")
                .param("tamanho", "1")
                .header("Authorization", "Bearer " + tokenGestor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].relatoId").value(semFoto))
        .andExpect(jsonPath("$.content[0].situacao").value("SEM_FOTO"))
        .andExpect(jsonPath("$.page.totalElements").value(1));
    verify(fileStorage, never()).existe(anyString());
  }

  @Test
  void cidadaoNaoConsultaDisponibilidadeDaGestao() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/gestao/relatos/disponibilidade-fotos")
                .header("Authorization", "Bearer " + cidadaos.token(cidadao)))
        .andExpect(status().isForbidden());
    verify(fileStorage, never()).existe(anyString());
  }

  private Integer criarCategoria(String prefixo) {
    return categoriaService
        .criar(new CategoriaRegistroDTO(prefixo + " " + UUID.randomUUID(), "foto"))
        .getId();
  }

  private Long registrar(int categoriaId, byte[] foto) {
    return relatoService
        .registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(categoriaId, "Relato com mídia", -6.08, -38.38),
            foto,
            foto == null ? null : "foto.jpg",
            foto == null ? null : "image/jpeg")
        .getId();
  }
}

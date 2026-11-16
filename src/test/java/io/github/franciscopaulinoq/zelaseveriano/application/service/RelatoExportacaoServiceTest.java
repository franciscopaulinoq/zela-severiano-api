package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.exportacao.CsvEscritorPadrao;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RelatoExportacaoServiceTest {
  @Mock private RelatoRepository relatoRepository;
  private RelatoService relatoService;

  @BeforeEach
  void setUp() {
    relatoService =
        new RelatoService(relatoRepository, null, null, null, null, new CsvEscritorPadrao());
  }

  @Test
  void percorrePaginasAteEsgotarOsResultadosSemMaterializarTudoNaConsulta() throws Exception {
    List<Relato> relatos = LongStream.rangeClosed(1, 201).mapToObj(this::relato).toList();
    when(relatoRepository.findAll(any(), any(Pageable.class)))
        .thenAnswer(invocacao -> pagina(relatos, invocacao.getArgument(1)));

    ByteArrayOutputStream saida = new ByteArrayOutputStream();
    relatoService.exportarCsv(FiltroRelatos.semFiltro(), saida);

    String[] linhas = saida.toString(StandardCharsets.UTF_8).split("\\r\\n");
    assertThat(linhas).hasSize(202);
    assertThat(linhas[1]).startsWith("1,Autor 1,Categoria 1,PENDENTE,Descrição 1,,");
    assertThat(linhas[201]).startsWith("201,Autor 201,Categoria 201,PENDENTE,Descrição 201,,");
    ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
    verify(relatoRepository, times(2)).findAll(eq(FiltroRelatos.semFiltro()), paginacao.capture());
    assertThat(paginacao.getAllValues())
        .extracting(Pageable::getPageNumber, Pageable::getPageSize)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple(0, 200),
            org.assertj.core.groups.Tuple.tuple(1, 200));
  }

  @Test
  void paginaExatamenteCheiaNaoDisparaConsultaAdicional() throws Exception {
    List<Relato> relatos = LongStream.rangeClosed(1, 200).mapToObj(this::relato).toList();
    when(relatoRepository.findAll(any(), any(Pageable.class)))
        .thenAnswer(invocacao -> pagina(relatos, invocacao.getArgument(1)));

    ByteArrayOutputStream saida = new ByteArrayOutputStream();
    relatoService.exportarCsv(FiltroRelatos.semFiltro(), saida);

    assertThat(saida.toString(StandardCharsets.UTF_8).split("\\r\\n")).hasSize(201);
    verify(relatoRepository).findAll(any(), any(Pageable.class));
  }

  @Test
  void propagaFalhaDaEscritaDoDownload() {
    when(relatoRepository.findAll(any(), any(Pageable.class)))
        .thenAnswer(invocacao -> pagina(List.of(relato(1)), invocacao.getArgument(1)));
    java.io.OutputStream saida =
        new java.io.OutputStream() {
          @Override
          public void write(int valor) throws java.io.IOException {
            throw new java.io.IOException("falha no destino");
          }
        };

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> relatoService.exportarCsv(FiltroRelatos.semFiltro(), saida))
        .isInstanceOf(java.io.IOException.class)
        .hasMessage("falha no destino");
  }

  @Test
  void repassaFiltroEProduzSomenteCabecalhoQuandoNaoHaResultados() throws Exception {
    FiltroRelatos filtro = new FiltroRelatos(StatusRelato.CONCLUIDO, 17);
    when(relatoRepository.findAll(eq(filtro), any(Pageable.class)))
        .thenAnswer(invocacao -> pagina(List.of(), invocacao.getArgument(1)));

    ByteArrayOutputStream saida = new ByteArrayOutputStream();
    relatoService.exportarCsv(filtro, saida);

    assertThat(saida.toString(StandardCharsets.UTF_8))
        .isEqualTo("id,autor,categoria,status,descricao,observacao,criado_em\r\n");
    ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
    verify(relatoRepository).findAll(eq(filtro), paginacao.capture());
    assertThat(paginacao.getValue().getPageNumber()).isZero();
    assertThat(paginacao.getValue().getPageSize()).isEqualTo(200);
  }

  @Test
  void escreveObservacaoEDataNoFormatoIsoSemAlterarSeuConteudo() throws Exception {
    Relato relato = relato(9);
    relato.atualizarStatus(StatusRelato.EM_ANALISE, "Observação, com \"aspas\"\ne nova linha");
    when(relatoRepository.findAll(any(), any(Pageable.class)))
        .thenAnswer(invocacao -> pagina(List.of(relato), invocacao.getArgument(1)));

    ByteArrayOutputStream saida = new ByteArrayOutputStream();
    relatoService.exportarCsv(FiltroRelatos.semFiltro(), saida);

    String csv = saida.toString(StandardCharsets.UTF_8);
    assertThat(csv)
        .contains("9,Autor 9,Categoria 9,EM_ANALISE,Descrição 9,")
        .contains("\"Observação, com \"\"aspas\"\"\ne nova linha\"")
        .contains(relato.getCriadoEm().toString());
  }

  private Page<Relato> pagina(List<Relato> relatos, Pageable requisicao) {
    int inicio = Math.toIntExact(requisicao.getOffset());
    List<Relato> conteudo =
        inicio >= relatos.size()
            ? List.of()
            : relatos.subList(inicio, Math.min(inicio + requisicao.getPageSize(), relatos.size()));
    return new PageImpl<>(conteudo, requisicao, relatos.size());
  }

  private Relato relato(long id) {
    return Relato.builder()
        .id(id)
        .perfil(Perfil.builder().id(UUID.randomUUID()).nomeCompleto("Autor " + id).build())
        .categoria(Categoria.builder().id(1).nome("Categoria " + id).build())
        .status(StatusRelato.PENDENTE)
        .descricao("Descrição " + id)
        .criadoEm(OffsetDateTime.parse("2026-01-10T12:00:00Z"))
        .build();
  }
}

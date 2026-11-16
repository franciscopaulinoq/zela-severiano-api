package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoRelatoResultadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Usuario;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportacaoRelatoLegadoServiceTest {

  private static final String CPF = "52998224725";
  private static final String CABECALHO =
      "identificador_externo,cpf,categoria_id,descricao,latitude,longitude,status,criado_em";

  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @Mock private CategoriaRepository categoriaRepository;
  @InjectMocks private ImportacaoRelatoLegadoService service;
  @Captor private ArgumentCaptor<List<Relato>> relatosCaptor;

  @Test
  void importaCamposEntreAspasPreservandoStatusEData() throws IOException {
    prepararCadastros();
    when(relatoRepository.saveAll(anyList())).thenAnswer(invocacao -> invocacao.getArgument(0));
    String conteudo;
    try (InputStream input =
        getClass().getResourceAsStream("/fixtures/relatos-legados-validos.csv")) {
      conteudo = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }

    ImportacaoRelatoResultadoDTO resultado = service.importar(arquivo(conteudo));

    assertThat(resultado.totalImportado()).isEqualTo(2);
    verify(relatoRepository).saveAll(relatosCaptor.capture());
    List<Relato> relatos = relatosCaptor.getValue();
    assertThat(relatos).hasSize(2);
    assertThat(relatos.getFirst().getDescricao())
        .isEqualTo("Poste com fio solto, em frente à praça");
    assertThat(relatos.getFirst().getStatus()).isEqualTo(StatusRelato.CONCLUIDO);
    assertThat(relatos.getFirst().getCriadoEm())
        .isEqualTo(OffsetDateTime.parse("2020-01-02T03:04:05-03:00"));
    assertThat(relatos.get(1).getDescricao()).isEqualTo("Calçada com \"buraco\"");
    assertThat(relatos.get(1).getStatus()).isEqualTo(StatusRelato.PENDENTE);
  }

  @Test
  void reuneErrosPorLinhaENaoSalvaOsRegistrosValidos() {
    prepararCadastros();
    String csv =
        CABECALHO
            + "\nvalido,"
            + CPF
            + ",1,Relato válido,-6.08,-38.38,PENDENTE,2020-01-02T03:04:05-03:00"
            + "\ncolunas-insuficientes,123"
            + "\nsem-perfil,11144477735,1,Relato,-6.08,-38.38,PENDENTE,2020-01-02T03:04:05-03:00"
            + "\nsem-categoria,"
            + CPF
            + ",999,Relato,-6.08,-38.38,PENDENTE,2020-01-02T03:04:05-03:00"
            + "\nfora,"
            + CPF
            + ",1,Relato,0,0,PENDENTE,2020-01-02T03:04:05-03:00"
            + "\nstatus,"
            + CPF
            + ",1,Relato,-6.08,-38.38,INEXISTENTE,2020-01-02T03:04:05-03:00";

    assertThatThrownBy(() -> service.importar(arquivo(csv)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Linha 3")
        .hasMessageContaining("Linha 4")
        .hasMessageContaining("CPF sem perfil")
        .hasMessageContaining("Linha 5")
        .hasMessageContaining("categoria inexistente")
        .hasMessageContaining("Linha 6")
        .hasMessageContaining("fora do município")
        .hasMessageContaining("Linha 7")
        .hasMessageContaining("status desconhecido");
    verify(relatoRepository, never()).saveAll(anyList());
  }

  @Test
  void recusaIdentificadorExternoJaImportado() {
    prepararCadastros();
    when(relatoRepository.findByIdentificadorExterno("legado-1"))
        .thenReturn(Optional.of(Relato.builder().build()));
    String csv =
        CABECALHO
            + "\nlegado-1,"
            + CPF
            + ",1,Relato,-6.08,-38.38,PENDENTE,2020-01-02T03:04:05-03:00";

    assertThatThrownBy(() -> service.importar(arquivo(csv)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Linha 2: identificador externo duplicado");
    verify(relatoRepository, never()).saveAll(anyList());
  }

  private void prepararCadastros() {
    Categoria categoria = Categoria.builder().id(1).nome("Iluminação").build();
    Usuario usuario = Usuario.builder().id(UUID.randomUUID()).cpf(CPF).build();
    Perfil perfil = Perfil.builder().id(UUID.randomUUID()).usuario(usuario).build();
    when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
    when(perfilRepository.findAllByUsuarioCpfIn(anySet())).thenReturn(List.of(perfil));
    when(relatoRepository.findByIdentificadorExterno(anyString())).thenReturn(Optional.empty());
  }

  private static MockMultipartFile arquivo(String conteudo) {
    return new MockMultipartFile(
        "arquivo", "relatos.csv", "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
  }
}

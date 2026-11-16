package io.github.franciscopaulinoq.zelaseveriano.infrastructure.exportacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CsvEscritorTest {
  private final CsvEscritorPadrao escritor = new CsvEscritorPadrao();

  @Test
  void preservaVirgulasAspasQuebrasDeLinhaENulos() throws Exception {
    StringBuilder saida = new StringBuilder();

    escritor.escreverLinha(saida, Arrays.asList("simples", "a,b", "a\"b", "a\nb", null));

    assertThat(saida.toString()).isEqualTo("simples,\"a,b\",\"a\"\"b\",\"a\nb\",\r\n");
  }

  @Test
  void preservaRetornoDeCarroAcentuacaoECamposVazios() throws Exception {
    StringBuilder saida = new StringBuilder();

    escritor.escreverLinha(saida, Arrays.asList("a\rb", "Ação", ""));

    assertThat(saida.toString()).isEqualTo("\"a\rb\",Ação,\r\n");
  }

  @Test
  void dobraAspasMesmoQuandoElaEhTodoOConteudo() throws Exception {
    StringBuilder saida = new StringBuilder();

    escritor.escreverLinha(saida, Arrays.asList("\"", "texto"));

    assertThat(saida.toString()).isEqualTo("\"\"\"\",texto\r\n");
  }

  @Test
  void separaRegistrosPorQuebraPadronizada() throws Exception {
    StringBuilder saida = new StringBuilder();

    escritor.escreverLinha(saida, Arrays.asList("um", "dois"));
    escritor.escreverLinha(saida, Arrays.asList("três", "quatro"));

    assertThat(saida.toString()).isEqualTo("um,dois\r\ntrês,quatro\r\n");
  }
}

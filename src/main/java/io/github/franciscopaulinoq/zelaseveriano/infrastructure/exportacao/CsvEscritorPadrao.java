package io.github.franciscopaulinoq.zelaseveriano.infrastructure.exportacao;

import io.github.franciscopaulinoq.zelaseveriano.domain.exportacao.CsvEscritor;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvEscritorPadrao implements CsvEscritor {
  @Override
  public void escreverLinha(Appendable destino, List<String> campos) throws IOException {
    for (int indice = 0; indice < campos.size(); indice++) {
      if (indice > 0) {
        destino.append(',');
      }
      destino.append(escapar(campos.get(indice)));
    }
    destino.append("\r\n");
  }

  static String escapar(String valor) {
    if (valor == null) {
      return "";
    }
    if (valor.indexOf('"') >= 0
        || valor.indexOf(',') >= 0
        || valor.indexOf('\n') >= 0
        || valor.indexOf('\r') >= 0) {
      return '"' + valor.replace("\"", "\"\"") + '"';
    }
    return valor;
  }
}

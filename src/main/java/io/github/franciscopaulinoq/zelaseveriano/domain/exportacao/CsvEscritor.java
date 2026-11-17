package io.github.franciscopaulinoq.zelaseveriano.domain.exportacao;

import java.io.IOException;
import java.util.List;

public interface CsvEscritor {
  void escreverLinha(Appendable destino, List<String> campos) throws IOException;
}

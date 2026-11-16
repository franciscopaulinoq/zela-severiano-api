package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.LocalDate;

public record FiltroRelatos(
    StatusRelato status, Integer categoriaId, LocalDate dataInicial, LocalDate dataFinal) {

  public FiltroRelatos {
    if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
      throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
    }
  }

  public FiltroRelatos(StatusRelato status, Integer categoriaId) {
    this(status, categoriaId, null, null);
  }

  public static FiltroRelatos semFiltro() {
    return new FiltroRelatos(null, null);
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO.TotalPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.TotalRelatosPorStatus;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EstatisticasRelatosComposer {

  public EstatisticasRelatosDTO compor(
      long total,
      List<TotalRelatosPorStatus> totaisPorStatus,
      List<TotalRelatosPorCategoria> totaisPorCategoria) {
    Map<StatusRelato, Long> porStatus = new EnumMap<>(StatusRelato.class);
    for (StatusRelato status : StatusRelato.values()) {
      porStatus.put(status, 0L);
    }
    totaisPorStatus.forEach(item -> porStatus.put(item.status(), item.total()));

    List<TotalPorCategoria> porCategoria =
        totaisPorCategoria.stream()
            .map(item ->
                new TotalPorCategoria(item.categoriaId(), item.categoriaNome(), item.total()))
            .toList();

    return new EstatisticasRelatosDTO(total, porStatus, porCategoria);
  }
}

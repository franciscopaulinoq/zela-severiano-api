package io.github.franciscopaulinoq.zelaseveriano.domain.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Aviso;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.AvisoLeitura;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AvisoRepository {
  Aviso save(Aviso aviso);

  void save(AvisoLeitura leitura);

  void saveAll(List<AvisoLeitura> leituras);

  List<AvisoLeitura> findAllByPerfilId(UUID perfilId);

  List<AvisoLeitura> findAllByPerfilIdAndAvisoIdIn(UUID perfilId, Collection<Long> avisoIds);
}

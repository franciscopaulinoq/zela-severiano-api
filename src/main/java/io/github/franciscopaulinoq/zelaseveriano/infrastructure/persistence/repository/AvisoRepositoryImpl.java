package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Aviso;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.AvisoLeitura;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.AvisoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AvisoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.AvisoLeituraMapper;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.AvisoMapper;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AvisoRepositoryImpl implements AvisoRepository {
  private final JpaAvisoRepository jpaRepository;
  private final JpaAvisoLeituraRepository jpaLeituraRepository;
  private final AvisoMapper mapper;
  private final AvisoLeituraMapper leituraMapper;

  @Override
  public Aviso save(Aviso aviso) {
    AvisoEntity salvo = jpaRepository.save(mapper.map(aviso));
    return mapper.map(salvo);
  }

  @Override
  public void save(AvisoLeitura leitura) {
    jpaLeituraRepository.save(leituraMapper.map(leitura));
  }

  @Override
  public void saveAll(List<AvisoLeitura> leituras) {
    jpaLeituraRepository.saveAll(leituras.stream().map(leituraMapper::map).toList());
  }

  @Override
  public List<AvisoLeitura> findAllByPerfilId(UUID perfilId) {
    return jpaLeituraRepository.findAllByPerfilIdOrderByAvisoCriadoEmDesc(perfilId).stream()
        .map(leituraMapper::map)
        .toList();
  }

  @Override
  public List<AvisoLeitura> findAllByPerfilIdAndAvisoIdIn(
      UUID perfilId, Collection<Long> avisoIds) {
    return jpaLeituraRepository.findAllByPerfilIdAndAvisoIdIn(perfilId, avisoIds).stream()
        .map(leituraMapper::map)
        .toList();
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.repository;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.AuditoriaGestao;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.AuditoriaGestaoRepository;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity.AuditoriaGestaoEntity;
import io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.mapper.AuditoriaGestaoMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditoriaGestaoRepositoryImpl implements AuditoriaGestaoRepository {
  private final JpaAuditoriaGestaoRepository jpaRepository;
  private final AuditoriaGestaoMapper mapper;

  @Override
  public AuditoriaGestao save(AuditoriaGestao auditoria) {
    return mapper.map(jpaRepository.save(mapper.map(auditoria)));
  }

  @Override
  public List<AuditoriaGestao> consultar(UUID gestorId, String metodo) {
    Specification<AuditoriaGestaoEntity> specification =
        (root, query, builder) -> {
          var predicate = builder.conjunction();
          if (gestorId != null) {
            predicate = builder.and(predicate, builder.equal(root.get("gestorId"), gestorId));
          }
          if (metodo != null) {
            predicate = builder.and(predicate, builder.equal(root.get("metodo"), metodo));
          }
          return predicate;
        };
    Sort ordenacao = Sort.by(Sort.Order.desc("momento"), Sort.Order.desc("id"));
    return jpaRepository.findAll(specification, ordenacao).stream().map(mapper::map).toList();
  }
}

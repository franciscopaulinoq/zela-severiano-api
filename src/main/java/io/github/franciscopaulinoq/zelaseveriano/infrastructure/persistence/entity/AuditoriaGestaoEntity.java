package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auditoria_gestao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaGestaoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "gestor_id", nullable = false)
  private UUID gestorId;

  @Column(name = "metodo", nullable = false, length = 10)
  private String metodo;

  @Column(name = "caminho", nullable = false, length = 500)
  private String caminho;

  @Column(name = "momento", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime momento;

  @Column(name = "status", nullable = false)
  private Integer status;
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusVistoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "vistorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistoriaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "relato_id", nullable = false)
  private Long relatoId;

  @Column(name = "gestor_id", nullable = false)
  private UUID gestorId;

  @Column(name = "inicio", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime inicio;

  @Column(name = "fim", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime fim;

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private StatusVistoria status;

  @Column(name = "criado_em", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  @CreationTimestamp
  private OffsetDateTime criadoEm;
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "comentarios_relato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioRelatoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "relato_id", nullable = false)
  private RelatoEntity relato;

  @Column(name = "autor_id", nullable = false)
  private UUID autorId;

  @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
  private String texto;

  @CreationTimestamp
  @Column(name = "criado_em", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime criadoEm;
}

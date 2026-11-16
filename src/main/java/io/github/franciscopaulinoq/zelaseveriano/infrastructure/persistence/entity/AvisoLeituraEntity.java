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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aviso_leituras")
@Getter
@Setter
@NoArgsConstructor
public class AvisoLeituraEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aviso_id", nullable = false)
  private AvisoEntity aviso;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfil_id", nullable = false)
  private PerfilEntity perfil;

  @Column(name = "lida_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime lidaEm;
}

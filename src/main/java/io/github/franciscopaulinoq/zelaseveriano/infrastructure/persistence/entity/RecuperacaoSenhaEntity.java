package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "recuperacoes_senha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuperacaoSenhaEntity {
  @Id private UUID id;

  @Column(name = "usuario_id", nullable = false)
  private UUID usuarioId;

  @Column(name = "codigo", nullable = false, length = 64)
  private String codigo;

  @Column(name = "expira_em", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime expiraEm;

  @Column(name = "invalidado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime invalidadoEm;

  @Column(name = "criado_em", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  @CreationTimestamp
  private OffsetDateTime criadoEm;
}

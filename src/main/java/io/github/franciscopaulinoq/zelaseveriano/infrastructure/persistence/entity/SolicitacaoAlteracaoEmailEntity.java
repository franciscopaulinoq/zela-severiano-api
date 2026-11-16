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

@Entity
@Table(name = "confirmacoes_email")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoAlteracaoEmailEntity {
  @Id private UUID id;

  @Column(name = "perfil_id", nullable = false)
  private UUID perfilId;

  @Column(name = "email_solicitado", nullable = false)
  private String emailSolicitado;

  @Column(name = "codigo_hash", nullable = false, length = 64)
  private String codigoHash;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;

  @Column(name = "expira_em", nullable = false)
  private OffsetDateTime expiraEm;

  @Column(name = "consumido_em")
  private OffsetDateTime consumidoEm;
}

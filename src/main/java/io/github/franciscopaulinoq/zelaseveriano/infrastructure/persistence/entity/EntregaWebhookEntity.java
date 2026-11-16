package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "entregas_webhook")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregaWebhookEntity {
  @Id private UUID id;

  @Column(name = "webhook_id", nullable = false)
  private UUID webhookId;

  @Column(name = "historico_id", nullable = false)
  private long historicoId;

  @Column(name = "relato_id", nullable = false)
  private long relatoId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_anterior", nullable = false, length = 20)
  private StatusRelato statusAnterior;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_novo", nullable = false, length = 20)
  private StatusRelato statusNovo;

  @Column(name = "momento_mudanca", nullable = false)
  private OffsetDateTime momentoMudanca;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;

  @Column(nullable = false)
  private int tentativas;

  @Column(name = "proxima_tentativa_em", nullable = false)
  private OffsetDateTime proximaTentativaEm;

  @Column(name = "entregue_em")
  private OffsetDateTime entregueEm;

  @Column(name = "ultimo_erro", columnDefinition = "TEXT")
  private String ultimoErro;
}

package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EntregaWebhook {
  private UUID id;
  private UUID webhookId;
  private long historicoId;
  private long relatoId;
  private StatusRelato statusAnterior;
  private StatusRelato statusNovo;
  private OffsetDateTime momentoMudanca;
  private OffsetDateTime criadoEm;
  private int tentativas;
  private OffsetDateTime proximaTentativaEm;
  private OffsetDateTime entregueEm;
  private String ultimoErro;

  public boolean estaPendente(OffsetDateTime agora) {
    return entregueEm == null && !agora.isBefore(proximaTentativaEm);
  }

  public void registrarSucesso(OffsetDateTime agora) {
    entregueEm = agora;
    ultimoErro = null;
  }

  public void registrarFalha(OffsetDateTime agora, String erro) {
    tentativas++;
    long segundos = Math.min(60, 1L << Math.min(tentativas, 6));
    proximaTentativaEm = agora.plus(Duration.ofSeconds(segundos));
    ultimoErro =
        erro == null ? "Falha desconhecida" : erro.substring(0, Math.min(erro.length(), 1000));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.persistence.entity;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historico_status_relato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoStatusRelatoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "relato_id", nullable = false)
    private Long relatoId;

    @Column(name = "status_anterior", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusRelato statusAnterior;

    @Column(name = "status_novo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusRelato statusNovo;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "alterado_em", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime alteradoEm;
}

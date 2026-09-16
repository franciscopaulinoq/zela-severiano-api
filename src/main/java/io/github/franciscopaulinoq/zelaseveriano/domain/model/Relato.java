package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
@Builder
public class Relato {
    private Long id;
    private Perfil perfil;
    private Categoria categoria;
    private StatusRelato status;
    private String descricao;
    private String urlFoto;
    private Double latitude;
    private Double longitude;
    private String observacaoResolucao;
    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;

    private static final Map<StatusRelato, Set<StatusRelato>> TRANSICOES_PERMITIDAS = Map.of(
            StatusRelato.PENDENTE, Set.of(StatusRelato.EM_ANALISE, StatusRelato.REJEITADO),
            StatusRelato.EM_ANALISE, Set.of(StatusRelato.CONCLUIDO, StatusRelato.REJEITADO),
            StatusRelato.CONCLUIDO, Set.of(),
            StatusRelato.REJEITADO, Set.of()
    );

    public void atualizarStatus(StatusRelato novoStatus, String observacao) {
        Set<StatusRelato> permitidas = TRANSICOES_PERMITIDAS.getOrDefault(this.status, Set.of());
        if (!permitidas.contains(novoStatus)) {
            throw new TransicaoStatusInvalidaException(
                    "Não é possível mudar o status de %s para %s.".formatted(this.status, novoStatus));
        }

        if (novoStatus == StatusRelato.REJEITADO && (observacao == null || observacao.isBlank())) {
            throw new TransicaoStatusInvalidaException("É necessário informar uma observação ao rejeitar um relato.");
        }

        this.status = novoStatus;
        this.observacaoResolucao = observacao;
        this.atualizadoEm = OffsetDateTime.now();
    }
}

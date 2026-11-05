package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RelatoTransicaoStatusTest {

    private static final Map<StatusRelato, Set<StatusRelato>> PERMITIDAS = Map.of(
            StatusRelato.PENDENTE, Set.of(StatusRelato.EM_ANALISE, StatusRelato.REJEITADO),
            StatusRelato.EM_ANALISE, Set.of(StatusRelato.CONCLUIDO, StatusRelato.REJEITADO));

    static Stream<Arguments> transicoesPermitidas() {
        return todasAsCombinacoes().filter(RelatoTransicaoStatusTest::permitida);
    }

    static Stream<Arguments> transicoesProibidas() {
        return todasAsCombinacoes().filter(combinacao -> !permitida(combinacao));
    }

    @ParameterizedTest(name = "{0} -> {1} é aceita")
    @MethodSource("transicoesPermitidas")
    void transicaoPermitidaEhAceita(StatusRelato atual, StatusRelato novo) {
        Relato relato = relatoCom(atual);

        relato.atualizarStatus(novo, "Justificativa");

        assertThat(relato.getStatus()).isEqualTo(novo);
    }

    @ParameterizedTest(name = "{0} -> {1} é recusada")
    @MethodSource("transicoesProibidas")
    void transicaoProibidaEhRecusadaSemAlterarORelato(StatusRelato atual, StatusRelato novo) {
        Relato relato = relatoCom(atual);

        assertThatThrownBy(() -> relato.atualizarStatus(novo, "Justificativa"))
                .isInstanceOf(TransicaoStatusInvalidaException.class);

        assertThat(relato.getStatus()).isEqualTo(atual);
        assertThat(relato.getAtualizadoEm()).isNull();
        assertThat(relato.getObservacaoResolucao()).isNull();
    }

    @Test
    void pendenteVaiParaEmAnaliseSemJustificativa() {
        Relato relato = relatoCom(StatusRelato.PENDENTE);

        relato.atualizarStatus(StatusRelato.EM_ANALISE, null);

        assertThat(relato.getStatus()).isEqualTo(StatusRelato.EM_ANALISE);
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusRelato.class,
            names = {"PENDENTE", "EM_ANALISE"})
    void rejeitarSemJustificativaEhRecusado(StatusRelato atual) {
        for (String semJustificativa : new String[] {null, "", "   "}) {
            Relato relato = relatoCom(atual);

            assertThatThrownBy(() -> relato.atualizarStatus(StatusRelato.REJEITADO, semJustificativa))
                    .isInstanceOf(TransicaoStatusInvalidaException.class)
                    .hasMessageContaining("observação");
            assertThat(relato.getStatus()).isEqualTo(atual);
        }
    }

    @ParameterizedTest
    @EnumSource(value = StatusRelato.class)
    void relatoConcluidoNaoAceitaNenhumaMudanca(StatusRelato novo) {
        Relato relato = relatoCom(StatusRelato.CONCLUIDO);

        assertThatThrownBy(() -> relato.atualizarStatus(novo, "Justificativa"))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
        assertThat(relato.getStatus()).isEqualTo(StatusRelato.CONCLUIDO);
    }

    @Test
    void mudancaBemSucedidaRegistraADataDeAtualizacaoEAObservacao() {
        Relato relato = relatoCom(StatusRelato.EM_ANALISE);
        OffsetDateTime antes = OffsetDateTime.now();

        relato.atualizarStatus(StatusRelato.CONCLUIDO, "Buraco tapado");

        assertThat(relato.getObservacaoResolucao()).isEqualTo("Buraco tapado");
        assertThat(relato.getAtualizadoEm()).isAfterOrEqualTo(antes).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Em vistoria"})
    void observacaoOpcionalFicaRegistradaComoInformada(String observacao) {
        Relato relato = relatoCom(StatusRelato.PENDENTE);

        relato.atualizarStatus(StatusRelato.EM_ANALISE, observacao);

        assertThat(relato.getObservacaoResolucao()).isEqualTo(observacao);
    }

    private static Stream<Arguments> todasAsCombinacoes() {
        return Stream.of(StatusRelato.values())
                .flatMap(atual -> Stream.of(StatusRelato.values()).map(novo -> Arguments.of(atual, novo)));
    }

    private static boolean permitida(Arguments combinacao) {
        StatusRelato atual = (StatusRelato) combinacao.get()[0];
        StatusRelato novo = (StatusRelato) combinacao.get()[1];
        return PERMITIDAS.getOrDefault(atual, Set.of()).contains(novo);
    }

    private static Relato relatoCom(StatusRelato status) {
        return Relato.builder().id(1L).status(status).build();
    }
}

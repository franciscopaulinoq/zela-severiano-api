package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.TransicaoStatusInvalidaException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RelatoCancelamentoTest {

    @Test
    void relatoPendenteEhCancelado() {
        Relato relato = relatoCom(StatusRelato.PENDENTE);

        relato.cancelar();

        assertThat(relato.getStatus()).isEqualTo(StatusRelato.CANCELADO);
        assertThat(relato.getAtualizadoEm()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusRelato.class,
            names = {"PENDENTE"},
            mode = EnumSource.Mode.EXCLUDE)
    void relatoQueNaoEstaPendenteNaoPodeSerCancelado(StatusRelato status) {
        Relato relato = relatoCom(status);

        assertThatThrownBy(relato::cancelar).isInstanceOf(TransicaoStatusInvalidaException.class);
        assertThat(relato.getStatus()).isEqualTo(status);
    }

    @Test
    void gestaoNaoCancelaPelaMudancaDeStatus() {
        Relato relato = relatoCom(StatusRelato.PENDENTE);

        assertThatThrownBy(() -> relato.atualizarStatus(StatusRelato.CANCELADO, null))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
        assertThat(relato.getStatus()).isEqualTo(StatusRelato.PENDENTE);
    }

    private static Relato relatoCom(StatusRelato status) {
        return Relato.builder().id(1L).status(status).build();
    }
}

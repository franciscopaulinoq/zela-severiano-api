package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @Test
    void aceitaCpfComDigitosVerificadoresCorretos() {
        assertThat(Cpf.of("52998224725").valor()).isEqualTo("52998224725");
        assertThat(Cpf.isValido("11144477735")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    void rejeitaCpfComTodosOsDigitosIguais(String cpf) {
        assertThat(Cpf.isValido(cpf)).isFalse();
        assertThatThrownBy(() -> Cpf.of(cpf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "52998224715", "11144477700"})
    void rejeitaCpfComDigitosVerificadoresInvalidos(String cpf) {
        assertThatThrownBy(() -> Cpf.of(cpf)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"5299822472", "529982247250", "529.982.247-25", "5299822472a"})
    void rejeitaCpfComTamanhoOuCaracteresInvalidos(String cpf) {
        assertThat(Cpf.isValido(cpf)).isFalse();
    }

    @Test
    void usuarioUsaAMesmaValidacao() {
        assertThatThrownBy(() -> Usuario.builder().cpf("11111111111").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido.");
        assertThat(Usuario.builder().cpf("52998224725").build().getCpf()).isEqualTo("52998224725");
    }
}

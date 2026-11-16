package io.github.franciscopaulinoq.zelaseveriano.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReclassificacaoRelatosDTOTest {
  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void criarValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void fecharValidator() {
    validatorFactory.close();
  }

  @Test
  void aceitaLoteComCemIdentificadores() {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(ids(100), 1);

    assertThat(validator.validate(dados)).isEmpty();
  }

  @Test
  void recusaLoteComCentoEUmIdentificadores() {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(ids(101), 1);

    assertThat(validator.validate(dados))
        .singleElement()
        .extracting(violacao -> violacao.getPropertyPath().toString())
        .isEqualTo("relatoIds");
  }

  @Test
  void recusaListaVazia() {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(Set.of(), 1);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  @Test
  void recusaListaNula() {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(null, 1);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -1})
  void recusaIdDeRelatoNaoPositivo(Long relatoId) {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(Set.of(relatoId), 1);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  @Test
  void recusaIdDeRelatoNulo() {
    Set<Long> ids = new java.util.HashSet<>();
    ids.add(null);
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(ids, 1);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void recusaCategoriaNaoPositiva(Integer categoriaId) {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(Set.of(1L), categoriaId);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  @Test
  void recusaCategoriaNula() {
    ReclassificacaoRelatosDTO dados = new ReclassificacaoRelatosDTO(Set.of(1L), null);

    assertThat(validator.validate(dados)).isNotEmpty();
  }

  private static Set<Long> ids(int quantidade) {
    return LongStream.rangeClosed(1, quantidade).boxed().collect(Collectors.toSet());
  }
}

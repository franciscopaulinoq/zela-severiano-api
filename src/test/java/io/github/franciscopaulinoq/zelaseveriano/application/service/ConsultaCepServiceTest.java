package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ConsultaCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EnderecoCepDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CepNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.integration.CepGateway;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.EnderecoCep;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultaCepServiceTest {
  @Mock private CepGateway cepGateway;
  @InjectMocks private ConsultaCepService service;

  @Test
  void consultaComMascaraNormalizaCepEReutilizaResultado() {
    EnderecoCep endereco = new EnderecoCep("Rua Principal", "Centro", "Doutor Severiano", "RN");
    when(cepGateway.consultar("59680000")).thenReturn(Optional.of(endereco));

    EnderecoCepDTO primeiro = service.consultar(new ConsultaCepDTO("59680-000"));
    EnderecoCepDTO segundo = service.consultar(new ConsultaCepDTO("59680000"));

    assertThat(primeiro)
        .isEqualTo(new EnderecoCepDTO("Rua Principal", "Centro", "Doutor Severiano", "RN"));
    assertThat(segundo).isEqualTo(primeiro);
    verify(cepGateway, times(1)).consultar("59680000");
  }

  @Test
  void cepInexistenteSolicitaPreenchimentoManual() {
    when(cepGateway.consultar("59680999")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.consultar(new ConsultaCepDTO("59680-999")))
        .isInstanceOf(CepNaoEncontradoException.class)
        .hasMessageContaining("preenchimento manual");
  }
}

package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.AuditoriaGestao;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.AuditoriaGestaoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditoriaGestaoServiceTest {
  @Mock private AuditoriaGestaoRepository repository;
  @InjectMocks private AuditoriaGestaoService service;

  @Test
  void registroPreservaDadosDaOperacao() {
    UUID gestorId = UUID.randomUUID();

    service.registrar(gestorId, "POST", "/api/v1/gestao/categorias", 201);

    ArgumentCaptor<AuditoriaGestao> captor = ArgumentCaptor.forClass(AuditoriaGestao.class);
    verify(repository).save(captor.capture());
    AuditoriaGestao evento = captor.getValue();
    assertThat(evento.getGestorId()).isEqualTo(gestorId);
    assertThat(evento.getMetodo()).isEqualTo("POST");
    assertThat(evento.getCaminho()).isEqualTo("/api/v1/gestao/categorias");
    assertThat(evento.getStatus()).isEqualTo(201);
    assertThat(evento.getMomento()).isNotNull();
  }

  @Test
  void consultaNormalizaMetodoEMapeiaTodosOsCampos() {
    UUID gestorId = UUID.randomUUID();
    AuditoriaGestao evento =
        AuditoriaGestao.builder()
            .id(10L)
            .gestorId(gestorId)
            .metodo("POST")
            .caminho("/api/v1/gestao/categorias")
            .momento(OffsetDateTime.now())
            .status(400)
            .build();
    when(repository.consultar(gestorId, "POST")).thenReturn(List.of(evento));

    var resultado = service.consultar(gestorId, "post");

    verify(repository).consultar(gestorId, "POST");
    assertThat(resultado)
        .singleElement()
        .satisfies(dto -> {
          assertThat(dto.id()).isEqualTo(10L);
          assertThat(dto.gestorId()).isEqualTo(gestorId);
          assertThat(dto.metodo()).isEqualTo("POST");
          assertThat(dto.caminho()).isEqualTo(evento.getCaminho());
          assertThat(dto.momento()).isEqualTo(evento.getMomento());
          assertThat(dto.status()).isEqualTo(400);
        });
  }
}

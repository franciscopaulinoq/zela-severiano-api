package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.MeuResumoRelatoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeuResumoRelatoServiceTest {

  @Mock private RelatoRepository relatoRepository;
  @Mock private PerfilRepository perfilRepository;
  @InjectMocks private MeuResumoRelatoService service;

  @Test
  void contaSomenteOsRelatosDoPerfilDoUsuario() {
    UUID usuarioId = UUID.randomUUID();
    UUID perfilId = UUID.randomUUID();
    when(perfilRepository.findByUsuarioId(usuarioId))
        .thenReturn(Optional.of(Perfil.builder().id(perfilId).build()));
    when(relatoRepository.countByPerfilId(perfilId)).thenReturn(7L);

    MeuResumoRelatoDTO resultado = service.obterResumo(usuarioId);

    assertThat(resultado.total()).isEqualTo(7);
    verify(relatoRepository).countByPerfilId(perfilId);
  }
}

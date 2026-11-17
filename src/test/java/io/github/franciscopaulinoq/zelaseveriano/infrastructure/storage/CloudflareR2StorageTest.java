package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.ArmazenamentoIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
class CloudflareR2StorageTest {
  private static final String URL = "https://arquivos.example.com/foto.jpg";

  @Mock private S3Client s3Client;
  @Mock private ResponseInputStream<GetObjectResponse> resposta;
  private CloudflareR2Storage storage;

  @BeforeEach
  void setUp() {
    storage = new CloudflareR2Storage(s3Client);
    ReflectionTestUtils.setField(storage, "bucketName", "relatos-teste");
    ReflectionTestUtils.setField(storage, "publicUrl", "https://arquivos.example.com");
  }

  @Test
  void baixaPelaChaveDaUrlEPreservaTipoDeConteudo() throws Exception {
    byte[] bytes = {1, 2, 3};
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(resposta);
    when(resposta.readAllBytes()).thenReturn(bytes);
    when(resposta.response())
        .thenReturn(GetObjectResponse.builder().contentType("image/jpeg").build());

    var arquivo = storage.baixar(URL);

    assertThat(arquivo.conteudo()).isEqualTo(bytes);
    assertThat(arquivo.tipoConteudo()).isEqualTo("image/jpeg");
    ArgumentCaptor<GetObjectRequest> requisicao = ArgumentCaptor.forClass(GetObjectRequest.class);
    org.mockito.Mockito.verify(s3Client).getObject(requisicao.capture());
    assertThat(requisicao.getValue().bucket()).isEqualTo("relatos-teste");
    assertThat(requisicao.getValue().key()).isEqualTo("foto.jpg");
  }

  @Test
  void objetoAusenteViraRecursoNaoEncontrado() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenThrow(NoSuchKeyException.builder().message("ausente").build());

    assertThatThrownBy(() -> storage.baixar(URL)).isInstanceOf(RecursoNaoEncontradoException.class);
  }

  @Test
  void falhaDoClienteViraIndisponibilidade() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenThrow(SdkClientException.create("indisponível"));

    assertThatThrownBy(() -> storage.baixar(URL))
        .isInstanceOf(ArmazenamentoIndisponivelException.class);
  }
}

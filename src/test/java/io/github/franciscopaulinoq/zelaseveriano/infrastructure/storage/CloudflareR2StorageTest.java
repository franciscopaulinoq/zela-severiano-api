package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class CloudflareR2StorageTest {
  @Mock private S3Client s3Client;
  private CloudflareR2Storage storage;

  @BeforeEach
  void setUp() {
    storage = new CloudflareR2Storage(s3Client);
    ReflectionTestUtils.setField(storage, "bucketName", "fotos");
    ReflectionTestUtils.setField(storage, "publicUrl", "https://cdn.example.com/base");
  }

  @Test
  void objetoEncontradoConfirmaExistenciaPelaChaveDaUrl() {
    when(s3Client.headObject(any(HeadObjectRequest.class)))
        .thenReturn(HeadObjectResponse.builder().build());

    assertThat(storage.existe("https://cdn.example.com/base/pasta/foto.jpg")).isTrue();

    ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
    verify(s3Client).headObject(captor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("fotos");
    assertThat(captor.getValue().key()).isEqualTo("pasta/foto.jpg");
  }

  @Test
  void resposta404IndicaObjetoAusente() {
    when(s3Client.headObject(any(HeadObjectRequest.class)))
        .thenThrow(S3Exception.builder().statusCode(404).build());

    assertThat(storage.existe("https://cdn.example.com/base/ausente.jpg")).isFalse();
  }
}

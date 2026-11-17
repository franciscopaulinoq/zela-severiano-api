package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class CloudflareR2StorageTest {
  @Mock private S3Client s3Client;
  @InjectMocks private CloudflareR2Storage storage;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(storage, "bucketName", "relatos-teste");
    ReflectionTestUtils.setField(storage, "publicUrl", "https://arquivos.example.com");
  }

  @Test
  void uploadMantemBucketChaveTipoEUrlPublica() {
    String url = storage.upload(new byte[] {1, 2}, "foto.jpg", "image/jpeg");

    ArgumentCaptor<PutObjectRequest> requisicao = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(requisicao.capture(), any(RequestBody.class));
    assertThat(requisicao.getValue().bucket()).isEqualTo("relatos-teste");
    assertThat(requisicao.getValue().key()).isEqualTo("foto.jpg");
    assertThat(requisicao.getValue().contentType()).isEqualTo("image/jpeg");
    assertThat(url).isEqualTo("https://arquivos.example.com/foto.jpg");
  }

  @Test
  void exclusaoUsaBucketEChaveDoObjeto() {
    storage.excluir("https://arquivos.example.com/550e8400-e29b-41d4-a716-446655440000.jpg");

    ArgumentCaptor<DeleteObjectRequest> requisicao =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(requisicao.capture());
    assertThat(requisicao.getValue().bucket()).isEqualTo("relatos-teste");
    assertThat(requisicao.getValue().key()).isEqualTo("550e8400-e29b-41d4-a716-446655440000.jpg");
  }

  @Test
  void exclusaoRecusaUrlDeOutroArmazenamento() {
    assertThatThrownBy(() -> storage.excluir("https://externo.example.com/foto.jpg"))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(s3Client);
  }
}

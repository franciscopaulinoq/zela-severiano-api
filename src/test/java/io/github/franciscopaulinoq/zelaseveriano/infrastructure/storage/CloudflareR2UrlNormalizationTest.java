package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
class CloudflareR2UrlNormalizationTest {
  @Mock private S3Client s3Client;
  private CloudflareR2Storage storage;

  @BeforeEach
  void setUp() {
    storage = new CloudflareR2Storage(s3Client);
    ReflectionTestUtils.setField(storage, "bucketName", "fotos");
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://cdn.example.com/base", "https://cdn.example.com/base/"})
  void uploadUsaUmaBarraEntreBaseEChave(String basePublica) {
    ReflectionTestUtils.setField(storage, "publicUrl", basePublica);

    String url = storage.upload(new byte[] {1}, "pasta/foto.jpg", "image/jpeg");

    assertThat(url).isEqualTo("https://cdn.example.com/base/pasta/foto.jpg");
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class CloudflareR2Storage implements FileStorage {

  private final S3Client s3Client;

  @Value("${cloudflare.r2.bucket-name}")
  private String bucketName;

  @Value("${cloudflare.r2.public-url}")
  private String publicUrl;

  @Override
  public String upload(byte[] bytes, String fileName, String contentType) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

    return publicUrl + "/" + fileName;
  }

  @Override
  public void excluir(String urlFoto) {
    String prefixo = publicUrl + "/";
    if (urlFoto == null || !urlFoto.startsWith(prefixo) || urlFoto.length() == prefixo.length()) {
      throw new IllegalArgumentException(
          "A URL da foto não pertence ao armazenamento configurado.");
    }
    String fileName = urlFoto.substring(prefixo.length());
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build();
    s3Client.deleteObject(deleteObjectRequest);
  }
}

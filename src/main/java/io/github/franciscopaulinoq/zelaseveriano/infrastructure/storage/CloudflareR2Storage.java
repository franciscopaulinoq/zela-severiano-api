package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
  public boolean existe(String urlFoto) {
    String prefixo = publicUrl.endsWith("/") ? publicUrl : publicUrl + "/";
    if (!urlFoto.startsWith(prefixo)) {
      throw new IllegalArgumentException(
          "A URL da foto não pertence ao armazenamento configurado.");
    }
    HeadObjectRequest request =
        HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(urlFoto.substring(prefixo.length()))
            .build();
    try {
      s3Client.headObject(request);
      return true;
    } catch (NoSuchKeyException ex) {
      return false;
    } catch (S3Exception ex) {
      if (ex.statusCode() == 404) {
        return false;
      }
      throw ex;
    }
  }
}

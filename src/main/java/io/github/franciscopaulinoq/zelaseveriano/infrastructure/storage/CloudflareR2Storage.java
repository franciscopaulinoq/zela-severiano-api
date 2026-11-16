package io.github.franciscopaulinoq.zelaseveriano.infrastructure.storage;

import io.github.franciscopaulinoq.zelaseveriano.domain.exception.ArmazenamentoIndisponivelException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.ArquivoArmazenado;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
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
  public ArquivoArmazenado baixar(String url) {
    GetObjectRequest request =
        GetObjectRequest.builder().bucket(bucketName).key(extrairChave(url)).build();
    try (ResponseInputStream<GetObjectResponse> resposta = s3Client.getObject(request)) {
      return new ArquivoArmazenado(resposta.readAllBytes(), resposta.response().contentType());
    } catch (NoSuchKeyException exception) {
      throw new RecursoNaoEncontradoException("Foto não encontrada.");
    } catch (SdkClientException | IOException exception) {
      throw new ArmazenamentoIndisponivelException(
          "Não foi possível baixar a foto no armazenamento.", exception);
    }
  }

  private String extrairChave(String url) {
    String prefixo = publicUrl + "/";
    if (url == null || !url.startsWith(prefixo) || url.length() == prefixo.length()) {
      throw new RecursoNaoEncontradoException("Foto não encontrada.");
    }
    return url.substring(prefixo.length());
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RequisicaoLoginWrapper extends HttpServletRequestWrapper {
  static final int TAMANHO_MAXIMO = 16 * 1024;

  private final byte[] conteudo;

  RequisicaoLoginWrapper(HttpServletRequest request) throws IOException {
    super(request);
    conteudo = request.getInputStream().readNBytes(TAMANHO_MAXIMO + 1);
  }

  boolean excedeTamanhoMaximo() {
    return conteudo.length > TAMANHO_MAXIMO;
  }

  byte[] conteudo() {
    return conteudo.clone();
  }

  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream entrada = new ByteArrayInputStream(conteudo);
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return entrada.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {}

      @Override
      public int read() {
        return entrada.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
  }
}

package io.github.franciscopaulinoq.zelaseveriano.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class SmtpPropertiesIntegrationTest {
  @Autowired private JavaMailSenderImpl javaMailSender;
  @Autowired private SmtpEmailSender emailSender;

  @Test
  void configuracaoSmtpUsaPropriedadesDeAmbienteSemConexao() {
    assertThat(javaMailSender.getHost()).isEqualTo("localhost");
    assertThat(javaMailSender.getPort()).isEqualTo(1025);
    assertThat(javaMailSender.getUsername()).isEmpty();
    assertThat(javaMailSender.getPassword()).isEmpty();
    assertThat(javaMailSender.getProtocol()).isEqualTo("smtp");
    assertThat(javaMailSender.getJavaMailProperties().getProperty("mail.smtp.auth"))
        .isEqualTo("false");
    assertThat(javaMailSender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable"))
        .isEqualTo("false");
    assertThat(ReflectionTestUtils.getField(emailSender, "remetente"))
        .isEqualTo("no-reply@example.com");
  }
}

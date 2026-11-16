package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoginAuditFilterConfiguration {

  @Bean
  public FilterRegistrationBean<LoginAuditFilter> loginAuditFilter(LoginAuditService auditService) {
    FilterRegistrationBean<LoginAuditFilter> registro =
        new FilterRegistrationBean<>(new LoginAuditFilter(auditService));
    registro.addUrlPatterns("/api/v1/auth/login");
    registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
    return registro;
  }
}

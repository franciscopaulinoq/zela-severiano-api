package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class RecuperacaoSenhaSecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain recuperacaoSenhaSecurityFilterChain(HttpSecurity http)
      throws Exception {
    return http.securityMatcher(
            "/api/v1/auth/recuperacao/solicitar", "/api/v1/auth/recuperacao/confirmar")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .build();
  }
}

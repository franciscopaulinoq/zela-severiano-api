package io.github.franciscopaulinoq.zelaseveriano.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class IntegracaoSecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain integracaoSecurityFilterChain(
      HttpSecurity http, @Value("${api.integracoes.estatisticas.api-key}") String apiKey)
      throws Exception {
    return http.securityMatcher("/api/v1/integracoes/estatisticas")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("INTEGRACAO"))
        .exceptionHandling(exceptions ->
            exceptions.authenticationEntryPoint((request, response, exception) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
        .addFilterBefore(new ApiKeyFilter(apiKey), UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}

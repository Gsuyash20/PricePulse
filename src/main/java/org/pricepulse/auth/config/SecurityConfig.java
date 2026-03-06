package org.pricepulse.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  /*
    * Using Argon2 for password encoding, which is a modern and secure hashing algorithm.
    * The defaultsForSpringSecurity_v5_8 method provides a secure configuration that is compatible with Spring Security 5.8 and later.
    * This ensures that passwords are hashed securely before being stored in the database,
    *  providing protection against brute-force attacks and other common password vulnerabilities.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  /*
    * Configures the security filter chain for the application.
    * Disables CSRF protection since this is a stateless API, and configures authorization rules.
    * All requests to endpoints under "/api/v1/**" are permitted without authentication, while any other requests require authentication.
    * The session management is set to stateless, meaning that the application will not maintain any session state between requests.
   */
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return httpSecurity.build();
  }

}

package org.pricepulse.auth.config;

import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.security.JwtAccessDeniedHandler;
import org.pricepulse.auth.security.JwtAuthenticationEntryPoint;
import org.pricepulse.auth.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
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
    * Disables CSRF protection since this is a stateless API that uses JWT for authentication.
    * Permits all requests to endpoints under "/public/**" and requires authentication for all other requests.
    * Configures custom handlers for access denied and authentication entry point exceptions.
    * Adds the JwtAuthenticationFilter before the UsernamePasswordAuthenticationFilter to ensure that JWT tokens are processed correctly.
    * Sets the session management policy to stateless, as JWT is used for authentication and no server-side sessions are maintained.
   */
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .accessDeniedHandler(jwtAccessDeniedHandler)
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return httpSecurity.build();
  }

}

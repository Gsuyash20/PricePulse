package org.pricepulse.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.security.JwtService;
import org.pricepulse.auth.service.CustomUserDetailsService;
import org.pricepulse.auth.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader(AuthRelatedEnum.AUTHORIZATION.getValue());
    final String jwtToken;
    final String userId;

    if (authHeader == null || !authHeader.startsWith(AuthRelatedEnum.BEARER.getValue() + " ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      jwtToken = authHeader.substring(AuthRelatedEnum.BEARER.getValue().length() + 1);
      String tokenType = jwtService.extractTokenType(jwtToken);

      if (!AuthRelatedEnum.ACCESS.name().equalsIgnoreCase(tokenType)) {
        throw new InvalidInputException("Invalid token type");
      }

      userId = jwtService.extractUserId(jwtToken);
      String role = jwtService.extractRole(jwtToken);

      if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null
          && Boolean.TRUE.equals(jwtService.isTokenValid(jwtToken, UUID.fromString(userId)))) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }

    } catch (Exception e) {
      log.warn("JWT validation failed: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}

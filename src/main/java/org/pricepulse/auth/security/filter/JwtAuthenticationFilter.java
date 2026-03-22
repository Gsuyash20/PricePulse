package org.pricepulse.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.security.JwtService;
import org.pricepulse.auth.service.CustomUserDetailsService;
import org.pricepulse.auth.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader(AuthRelatedEnum.AUTHORIZATION.getValue());
    final String jwtToken;
    final String userId;

    if(authHeader == null || !authHeader.startsWith(AuthRelatedEnum.BEARER.getValue() + " ")){
      filterChain.doFilter(request, response);
      return;
    }

    jwtToken = authHeader.substring(AuthRelatedEnum.BEARER.getValue().length() +1);
    userId = jwtService.extractUserId(jwtToken);

    if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails user = customUserDetailsService.loadUserByUserName(userId);
      if(user != null && jwtService.isTokenValid(jwtToken, UUID.fromString(userId))){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            user, null, Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }

    }
    filterChain.doFilter(request, response);
  }
}

package org.pricepulse.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pricepulse.auth.dto.response.ExceptionResponseDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(
        "Forbidden",
        403,
        accessDeniedException.getMessage(),
        request.getRequestURI(),
        Instant.now(),
        null
    );

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");
    response.getWriter().write(objectMapper.writeValueAsString(exceptionResponseDTO));
  }
}

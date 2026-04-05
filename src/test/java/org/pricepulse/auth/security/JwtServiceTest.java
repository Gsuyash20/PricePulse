package org.pricepulse.auth.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.domain.entity.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
  @InjectMocks
  private JwtService jwtService;

  @Mock
  private JwtConfigProperties jwtConfigProperties;

  @Test
  void testGenerateTokenShouldReturnNonNullToken() {
    String secret = "dGVzdC1zZWNyZXQtZm9yLWp3dC10ZXN0aW5nLXdoaWNoLW11c3QtYmUtYmFzZTY0LWVuY29kZWQ="; // base64 encoded test secret
    long expirationTime = 3600L;
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .role("USER")
        .build();

    when(jwtConfigProperties.getSecret()).thenReturn(secret);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    String token = jwtService.generateToken(user);

    assertNotNull(token);
  }

  @Test
  void testExtractUserIdShouldReturnCorrectUserId() {
    String secret = "dGVzdC1zZWNyZXQtZm9yLWp3dC10ZXN0aW5nLXdoaWNoLW11c3QtYmUtYmFzZTY0LWVuY29kZWQ=";
    long expirationTime = 3600L;
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .email("test@example.com")
        .role("USER")
        .build();

    when(jwtConfigProperties.getSecret()).thenReturn(secret);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    String token = jwtService.generateToken(user);
    String extractedUserId = jwtService.extractUserId(token);

    assertEquals(userId.toString(), extractedUserId);
  }

  @Test
  void testIsTokenValidWhenValidShouldReturnTrue() {
    String secret = "dGVzdC1zZWNyZXQtZm9yLWp3dC10ZXN0aW5nLXdoaWNoLW11c3QtYmUtYmFzZTY0LWVuY29kZWQ=";
    long expirationTime = 3600L;
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .email("test@example.com")
        .role("USER")
        .build();

    when(jwtConfigProperties.getSecret()).thenReturn(secret);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    String token = jwtService.generateToken(user);
    Boolean isValid = jwtService.isTokenValid(token, userId);

    assertTrue(isValid);
  }

  @Test
  void testIsTokenValidWhenInvalidUserIdShouldReturnFalse() {
    String secret = "dGVzdC1zZWNyZXQtZm9yLWp3dC10ZXN0aW5nLXdoaWNoLW11c3QtYmUtYmFzZTY0LWVuY29kZWQ=";
    long expirationTime = 3600L;
    UUID userId = UUID.randomUUID();
    UUID wrongUserId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .email("test@example.com")
        .role("USER")
        .build();

    when(jwtConfigProperties.getSecret()).thenReturn(secret);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    String token = jwtService.generateToken(user);
    Boolean isValid = jwtService.isTokenValid(token, wrongUserId);

    assertFalse(isValid);
  }
}

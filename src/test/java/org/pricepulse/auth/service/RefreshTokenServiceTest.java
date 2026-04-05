package org.pricepulse.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.RefreshToken;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.domain.repository.RefreshTokenRepository;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.security.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
  @InjectMocks
  private RefreshTokenService refreshTokenService;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private JwtConfigProperties jwtConfigProperties;

  private String hashToken(String rawToken) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException("Error hashing token");
    }
  }

  @Test
  void testCreateRefreshTokenShouldReturnRawToken() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .build();

    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(null);

    String rawToken = refreshTokenService.createRefreshToken(user);

    assertNotNull(rawToken);
    verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
  }

  @Test
  void testValidateRefreshTokenWhenValidShouldReturnRefreshToken() {
    String rawToken = "validToken";
    String tokenHash = hashToken(rawToken);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .build();
    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .expiryDate(Instant.now().plusSeconds(3600))
        .revoked(false)
        .build();

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

    RefreshToken result = refreshTokenService.validateRefreshToken(rawToken);

    assertEquals(refreshToken, result);
  }

  @Test
  void testValidateRefreshTokenWhenInvalidHashShouldThrowException() {
    String rawToken = "invalidToken";
    String tokenHash = hashToken(rawToken);

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> refreshTokenService.validateRefreshToken(rawToken));
  }

  @Test
  void testValidateRefreshTokenWhenExpiredShouldThrowException() {
    String rawToken = "expiredToken";
    String tokenHash = hashToken(rawToken);
    RefreshToken refreshToken = RefreshToken.builder()
        .tokenHash(tokenHash)
        .expiryDate(Instant.now().minusSeconds(3600))
        .revoked(false)
        .build();

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

    assertThrows(InvalidInputException.class, () -> refreshTokenService.validateRefreshToken(rawToken));
  }

  @Test
  void testValidateRefreshTokenWhenRevokedShouldThrowException() {
    String rawToken = "revokedToken";
    String tokenHash = hashToken(rawToken);
    RefreshToken refreshToken = RefreshToken.builder()
        .tokenHash(tokenHash)
        .expiryDate(Instant.now().plusSeconds(3600))
        .revoked(true)
        .build();

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

    assertThrows(InvalidInputException.class, () -> refreshTokenService.validateRefreshToken(rawToken));
  }

  @Test
  void testRefreshTokenShouldReturnNewLoginResponse() {
    String refreshTokenStr = "refreshToken";
    String tokenHash = hashToken(refreshTokenStr);
    String newAccessToken = "newAccessToken";
    long expirationTime = 3600L;
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .build();
    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .expiryDate(Instant.now().plusSeconds(3600))
        .revoked(false)
        .build();

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
    when(jwtService.generateToken(user)).thenReturn(newAccessToken);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    var response = refreshTokenService.refreshToken(refreshTokenStr);

    assertNotNull(response);
    assertEquals(newAccessToken, response.accessToken());
    assertEquals(refreshTokenStr, response.refreshToken());
    assertEquals(AuthRelatedEnum.BEARER.name(), response.tokenType());
    assertNotNull(response.expiresIn());
  }

  @Test
  void testLogOutUserShouldRevokeToken() {
    String refreshTokenStr = "refreshToken";
    String tokenHash = hashToken(refreshTokenStr);
    RefreshToken refreshToken = RefreshToken.builder()
        .tokenHash(tokenHash)
        .expiryDate(Instant.now().plusSeconds(3600))
        .revoked(false)
        .build();

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

    refreshTokenService.logOutUser(refreshTokenStr);

    assertTrue(refreshToken.isRevoked());
    verify(refreshTokenRepository, times(1)).save(refreshToken);
  }
}

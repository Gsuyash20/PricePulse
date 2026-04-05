package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.RefreshToken;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.domain.repository.RefreshTokenRepository;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final JwtConfigProperties jwtConfigProperties;

  public String createRefreshToken(User user) {

    String rawToken = UUID.randomUUID().toString();
    String tokenHash = hashToken(rawToken);

    RefreshToken token = RefreshToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .createdAt(Instant.now())
        .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
        .revoked(false)
        .build();

    refreshTokenRepository.save(token);

    return rawToken;
  }

  public RefreshToken validateRefreshToken(String token) {
    String tokenHash = hashToken(token);
    RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
      throw new InvalidInputException("Expired or revoked refresh token");
    }

    return refreshToken;
  }

  private String hashToken(String rawToken) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new InvalidInputException("Invalid token hash");
    }
  }

  public LoginResponseDTO refreshToken(String refreshToken) {
    RefreshToken token = validateRefreshToken(refreshToken);

    String newAccessToken = jwtService.generateToken(token.getUser());
    Instant expiresIn = Instant.now().plusSeconds(jwtConfigProperties.getExpirationTime());

    return new LoginResponseDTO(newAccessToken, refreshToken, AuthRelatedEnum.BEARER.name(), expiresIn, null);
  }


  public void logOutUser(String refreshToken) {
    RefreshToken token = validateRefreshToken(refreshToken);
    token.setRevoked(Boolean.TRUE);
    refreshTokenRepository.save(token);
  }
}

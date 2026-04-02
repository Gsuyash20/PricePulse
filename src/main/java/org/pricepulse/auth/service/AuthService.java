package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.config.SecurityConfig;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.RefreshToken;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final SecurityConfig securityConfig;
  private final JwtConfigProperties jwtConfigProperties;
  private final RefreshTokenService refreshTokenService;

  public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
    User existingUser = userRepository.findByEmail(loginRequestDTO.email()).orElseThrow(
        () -> new NotFoundException("Invalid credentials")
    );

    if (!securityConfig.passwordEncoder().matches(loginRequestDTO.password(), existingUser.getPasswordHash())) {
      log.error("Invalid password for user {}", loginRequestDTO.email());
      throw new InvalidInputException("Invalid credentials");
    }

    String token = jwtService.generateToken(existingUser);
    String refreshToken = refreshTokenService.createRefreshToken(existingUser);
    Instant expiresIn = Instant.now().plusSeconds(jwtConfigProperties.getExpirationTime());
    return new LoginResponseDTO(token, refreshToken, AuthRelatedEnum.BEARER.name(), expiresIn);

  }
}

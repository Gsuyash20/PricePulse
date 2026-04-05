package org.pricepulse.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @InjectMocks
  private AuthService authService;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtConfigProperties jwtConfigProperties;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  void testLoginUserWhenValidCredentialsShouldReturnLoginResponse() {
    String email = "test@example.com";
    String password = "password123";
    String hashedPassword = "hashedPassword";
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";
    long expirationTime = 3600L;
    Instant expiresIn = Instant.now().plusSeconds(expirationTime);

    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .passwordHash(hashedPassword)
        .role("USER")
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn(accessToken);
    when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
    when(jwtConfigProperties.getExpirationTime()).thenReturn(expirationTime);

    LoginResponseDTO response = authService.loginUser(requestDTO);

    assertNotNull(response);
    assertEquals(accessToken, response.accessToken());
    assertEquals(refreshToken, response.refreshToken());
    assertEquals(AuthRelatedEnum.BEARER.name(), response.tokenType());
    assertNotNull(response.expiresIn());
  }

  @Test
  void testLoginUserWhenUserNotFoundShouldThrowNotFoundException() {
    String email = "nonexistent@example.com";
    String password = "password123";
    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> authService.loginUser(requestDTO));
  }

  @Test
  void testLoginUserWhenInvalidPasswordShouldThrowInvalidInputException() {
    String email = "test@example.com";
    String password = "wrongpassword";
    String hashedPassword = "hashedPassword";
    User user = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .passwordHash(hashedPassword)
        .role("USER")
        .build();

    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

    assertThrows(InvalidInputException.class, () -> authService.loginUser(requestDTO));
  }
}

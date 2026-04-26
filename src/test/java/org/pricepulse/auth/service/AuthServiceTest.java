package org.pricepulse.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.context.PulseContext;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

  @Mock
  private RateLimitService rateLimitService;

  @Mock
  private AuditService auditService;

  @Test
  void testLoginUserWhenValidCredentialsShouldReturnLoginResponse() {
    String email = "test@example.com";
    String password = "password123";
    String hashedPassword = "hashedPassword";
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";
    long expirationTime = 3600L;
    UUID userId = UUID.randomUUID();
    String emailKey = "login:user:" + email;

    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);
    User user = User.builder()
        .id(userId)
        .email(email)
        .passwordHash(hashedPassword)
        .role("USER")
        .build();

    PulseContext pulseContext = PulseContext.builder()
        .ipAddress("127.0.0.1")
        .userAgent("TestAgent")
        .traceId("trace123")
        .requestId("req123")
        .userId(userId)
        .build();

    Authentication authentication = mock(Authentication.class);
    when(authentication.getDetails()).thenReturn(pulseContext);

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

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
      assertEquals(userId, response.userId());

      verify(rateLimitService).validateLoginAttempt(emailKey);
      verify(rateLimitService).resetAttempts(emailKey);
      verify(auditService).log("LOGIN_SUCCESS", userId, email, "127.0.0.1", "TestAgent", null, "trace123");
    }
  }

  @Test
  void testLoginUserWhenUserNotFoundShouldThrowInvalidInputException() {
    String email = "nonexistent@example.com";
    String password = "password123";
    String emailKey = "login:user:" + email;
    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    InvalidInputException exception = assertThrows(InvalidInputException.class, () -> authService.loginUser(requestDTO));
    assertEquals("Invalid credentials", exception.getMessage());

    verify(rateLimitService).validateLoginAttempt(emailKey);
    verify(rateLimitService).recordFailedAttempts(emailKey);
  }

  @Test
  void testLoginUserWhenInvalidPasswordShouldThrowInvalidInputException() {
    String email = "test@example.com";
    String password = "wrongpassword";
    String hashedPassword = "hashedPassword";
    String emailKey = "login:user:" + email;
    User user = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .passwordHash(hashedPassword)
        .role("USER")
        .build();

    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

    InvalidInputException exception = assertThrows(InvalidInputException.class, () -> authService.loginUser(requestDTO));
    assertEquals("Invalid credentials", exception.getMessage());

    verify(rateLimitService).validateLoginAttempt(emailKey);
    verify(rateLimitService, times(2)).recordFailedAttempts(emailKey);
  }

  @Test
  void testLoginUserWhenRateLimitExceededShouldThrowInvalidInputException() {
    String email = "test@example.com";
    String password = "password123";
    String emailKey = "login:user:" + email;
    LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);

    doThrow(new InvalidInputException("Too many login attempts. Try again later")).when(rateLimitService).validateLoginAttempt(emailKey);

    InvalidInputException exception = assertThrows(InvalidInputException.class, () -> authService.loginUser(requestDTO));
    assertEquals("Too many login attempts. Try again later", exception.getMessage());

    verify(rateLimitService).validateLoginAttempt(emailKey);
  }
}

package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.AuditEventType;
import org.pricepulse.auth.constants.AuthRelatedEnum;
import org.pricepulse.auth.context.PulseContext;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.pricepulse.auth.security.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;
  private final JwtConfigProperties jwtConfigProperties;
  private final RefreshTokenService refreshTokenService;
  private final AuditService auditService;

  public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
    User existingUser = userRepository.findByEmail(loginRequestDTO.email()).orElseThrow(
        () -> new NotFoundException("Invalid credentials")
    );

    if (!passwordEncoder.matches(loginRequestDTO.password(), existingUser.getPasswordHash())) {
      log.error("Invalid password for user {}", loginRequestDTO.email());
      throw new InvalidInputException("Invalid credentials");
    }

    String token = jwtService.generateToken(existingUser);
    String refreshToken = refreshTokenService.createRefreshToken(existingUser);
    Instant expiresIn = Instant.now().plusSeconds(jwtConfigProperties.getExpirationTime());

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getDetails() instanceof PulseContext context) {
      auditService.log(AuditEventType.LOGIN_SUCCESS.name(), existingUser.getId(), existingUser.getEmail(),
          context.getIpAddress(), context.getUserAgent(), null, context.getTraceId());
    }

    return new LoginResponseDTO(token, refreshToken, AuthRelatedEnum.BEARER.name(), expiresIn, existingUser.getId());

  }
}

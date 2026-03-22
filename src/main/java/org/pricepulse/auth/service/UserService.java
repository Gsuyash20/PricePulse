package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.SecurityConfig;
import org.pricepulse.auth.constants.MessageConstants;
import org.pricepulse.auth.constants.UserRolesEnum;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.exception.generic.DuplicateResourceException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final SecurityConfig securityConfig;

  @Transactional(readOnly = true)
  public User fetchUserByEmail(String email) {
    log.info("Fetching User {}", email);
    return userRepository.findByEmail(email).orElseThrow(
        () -> new NotFoundException("User with email " + email + " not found")
    );
  }

  @Transactional(readOnly = true)
  public User fetchUserById(String userId) {
    return userRepository.findById(UUID.fromString(userId)).orElse(null);
  }

  public RegisterResponseDTO createUser(RegisterRequestDTO requestDTO) {
    if (userRepository.existsByEmail(requestDTO.email())) {
      log.error("User with email {} already exists", requestDTO.email());
      throw new DuplicateResourceException("User with email " + requestDTO.email() + " already exists");
    }
    String hashPassword = securityConfig.passwordEncoder().encode(requestDTO.password());

    User user = User.builder()
        .createdAt(Instant.now())
        .email(requestDTO.email())
        .role(UserRolesEnum.USER.name())
        .enabled(Boolean.TRUE)
        .passwordHash(hashPassword)
        .accountNonLocked(Boolean.TRUE)
        .build();
    userRepository.save(user);

    return new RegisterResponseDTO(requestDTO.email(), MessageConstants.USER_CREATED_SUCCESSFULLY, user.getId());

  }
}

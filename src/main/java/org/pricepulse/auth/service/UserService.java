package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.SecurityConfig;
import org.pricepulse.auth.constants.MessageConstants;
import org.pricepulse.auth.constants.UserRolesEnum;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final SecurityConfig securityConfig;

  public User saveUser(User user) {
    log.info("Saving User {}", user.getEmail());
    if(userRepository.existsByEmail(user.getEmail())) {
      log.info("User with email {} already exists", user.getEmail());
      throw new RuntimeException("User with email " + user.getEmail() + " already exists");
    }
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User fetchUserByEmail(String email) {
    log.info("Fetching User {}", email);
    return userRepository.findByEmail(email).orElseThrow(
        () -> new RuntimeException("User with email " + email + " not found")
    );
  }

  public RegisterResponseDTO createUser(RegisterRequestDTO requestDTO) {
    if (userRepository.existsByEmail(requestDTO.email())) {
      log.error("User with email {} already exists", requestDTO.email());
      throw new RuntimeException("User with email " + requestDTO.email() + " already exists");
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

package org.pricepulse.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.config.SecurityConfig;
import org.pricepulse.auth.constants.MessageConstants;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.exception.generic.DuplicateResourceException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SecurityConfig securityConfig;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  void testFetchUserByEmailWhenUserExistShouldReturnUser() {
    String email = "abc@yahoo.com";
    User expectedUser = User.builder()
        .email(email)
        .role("USER")
        .createdAt(Instant.now())
        .build();
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(expectedUser));

    User actualUser = userService.fetchUserByEmail(email);

    Assertions.assertNotNull(email, "Email should not be null");
    Assertions.assertEquals(expectedUser, actualUser, "Expected and actual user should be the same instance");
  }

  @Test
  void testFetchUserByEmailWhenUserNotExistThenThrowException() {
    String email = "";
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> userService.fetchUserByEmail(email), "Exception not thrown");
  }

  @Test
  void testCreateUserWhenUserExistShouldThrowException(){
    RegisterRequestDTO requestDTO = new RegisterRequestDTO("abcd@gmail.com", "123456789");
    when(userRepository.existsByEmail(any())).thenReturn(Boolean.TRUE);

    assertThrows(DuplicateResourceException.class,
        () -> userService.createUser(requestDTO), "Exception not thrown");
  }

  @Test
  void testCreateUserWhenUserNotExistThenCreateUser() {
    String email = "abcd@gmail.com";
    RegisterRequestDTO requestDTO = new RegisterRequestDTO(email, "123456789");
    RegisterResponseDTO expectedResponse = new RegisterResponseDTO(email, MessageConstants.USER_CREATED_SUCCESSFULLY, UUID.randomUUID());
    User expectedUser = User.builder()
        .email(email)
        .role("USER")
        .createdAt(Instant.now())
        .build();

    when(userRepository.existsByEmail(any())).thenReturn(Boolean.FALSE);
    when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);
    when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
    when(userRepository.save(any())).thenReturn(expectedUser);

    RegisterResponseDTO actualResponse = userService.createUser(requestDTO);

    assertEquals(expectedResponse.email(), actualResponse.email(), "Expected and actual response should be the same instance");
  }
}

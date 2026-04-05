package org.pricepulse.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  @Mock
  private UserRepository userRepository;

  @Test
  void testLoadUserByUserNameWhenUserExistsShouldReturnUserDetails() {
    String userId = UUID.randomUUID().toString();
    User user = User.builder()
        .id(UUID.fromString(userId))
        .email("test@example.com")
        .passwordHash("hashedPassword")
        .role("USER")
        .build();

    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

    UserDetails userDetails = customUserDetailsService.loadUserByUserName(userId);

    assertNotNull(userDetails);
    assertEquals(user.getEmail(), userDetails.getUsername());
    assertEquals(user.getPasswordHash(), userDetails.getPassword());
  }

  @Test
  void testLoadUserByUserNameWhenUserNotFoundShouldThrowNotFoundException() {
    String userId = UUID.randomUUID().toString();

    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> customUserDetailsService.loadUserByUserName(userId));
  }
}

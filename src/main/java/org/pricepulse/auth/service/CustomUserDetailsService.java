package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.domain.repository.UserRepository;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {
  private final UserRepository userRepository;

  public UserDetails loadUserByUserName(String userId) {
    User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
        () -> new NotFoundException("User not found with id: " + userId)
    );
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        Collections.emptyList()
    );
  }

}

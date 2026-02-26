package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.domain.entity.User;
import org.pricepulse.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository userRepository;

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


}

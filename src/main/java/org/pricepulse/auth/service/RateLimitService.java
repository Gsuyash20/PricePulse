package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/*
  We wrap Redis calls with a circuit breaker (e.g., Resilience 4j).
  If Redis is unavailable, we fail open—skip rate limiting temporarily—to maintain service availability.
  Optionally, we can fall back to a local in-memory limiter for partial protection.
  This is a deliberate tradeoff prioritizing availability over strict security.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
  private final StringRedisTemplate redisTemplate;
  private static final int MAX_ATTEMPTS = 5;
  private static final long BLOCK_DURATION = 15; // in minutes

  public void validateLoginAttempt(String key) {
    String value = redisTemplate.opsForValue().get(key);

    if (value != null && Integer.parseInt(value) >= MAX_ATTEMPTS) {
      log.error("Too many login attempts. Try again later");
      throw new InvalidInputException("Too many login attempts. Try again later");
    }
  }

  public void recordFailedAttempts(String key) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1L) {
      redisTemplate.expire(key, Duration.ofMinutes(BLOCK_DURATION));
    }
  }

  public void resetAttempts(String key) {
    redisTemplate.delete(key);
  }

}

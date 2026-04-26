package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/*
 * Sliding window rate limiter using Redis ZSet.
 *
 * Each failed attempt is stored as a scored entry (score = timestamp in ms).
 * On each request:
 *   1. Remove all entries outside the current window (older than 15 min)
 *   2. Count remaining entries
 *   3. Reject if count >= MAX_ATTEMPTS
 *
 * Advantage over fixed window: no boundary burst attack.
 * A user cannot exhaust attempts at 14:59 and immediately get 5 more at 15:00.
 * We wrap Redis calls with a circuit breaker (e.g., Resilience 4j).
 * If Redis is unavailable, we fail open—skip rate limiting temporarily—to maintain service availability.
 * Optionally, we can fall back to a local in-memory limiter for partial protection.
 * This is a deliberate tradeoff prioritizing availability over strict security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

  private final StringRedisTemplate redisTemplate;

  private static final int MAX_ATTEMPTS = 5;
  private static final Duration WINDOW = Duration.ofMinutes(15);

  public void validateLoginAttempt(String key) {
    try {
      long now = System.currentTimeMillis();
      long windowStart = now - WINDOW.toMillis();

      // Evict attempts outside the sliding window
      redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

      Long attempts = redisTemplate.opsForZSet().zCard(key);

      if (attempts != null && attempts >= MAX_ATTEMPTS) {
        log.warn("Rate limit exceeded for key={}", key);
        throw new InvalidInputException("Too many login attempts. Try again later");
      }
    } catch (InvalidInputException ex) {
      throw ex; // rethrow — don't swallow rate limit rejections
    } catch (Exception ex) {
      // Redis unavailable — fail open
      log.error("Rate limiter unavailable, failing open. key={}", key, ex);
    }
  }

  public void recordFailedAttempts(String key) {
    try {
      long now = System.currentTimeMillis();
      // Score = timestamp, value = unique per attempt
      redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
      redisTemplate.expire(key, WINDOW);
    } catch (Exception ex) {
      log.error("Failed to record login attempt in Redis. key={}", key, ex);
    }
  }

  public void resetAttempts(String key) {
    try {
      redisTemplate.delete(key);
    } catch (Exception ex) {
      log.error("Failed to reset login attempts in Redis. key={}", key, ex);
    }
  }
}

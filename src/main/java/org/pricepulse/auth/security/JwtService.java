package org.pricepulse.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.domain.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JwtService {
  private final JwtConfigProperties jwtConfigProperties;

  private SecretKey getSigningKey(){
//    // In a real application, you should use a more secure way to generate and store the secret key
//    return Keys.hmacShaKeyFor(jwtConfigProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    log.debug("JWT secret length: {}", jwtConfigProperties.getSecret().length());
    byte[] keyBytes = Decoders.BASE64.decode(jwtConfigProperties.getSecret());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(User user){
    log.debug("JWT secret length: {}", jwtConfigProperties.getSecret().length());
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .claim("role", user.getRole())
        .issuedAt(Date.from(now))
        .expiration(Date.from(Instant.now().plusSeconds(jwtConfigProperties.getExpirationTime())))
        .signWith(getSigningKey())
        .compact();
  }
}

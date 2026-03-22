package org.pricepulse.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.config.JwtConfigProperties;
import org.pricepulse.auth.constants.JwtPayloadClaimsEnum;
import org.pricepulse.auth.domain.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

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
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim(JwtPayloadClaimsEnum.EMAIL.name(), user.getEmail())
        .claim(JwtPayloadClaimsEnum.ROLE.name(), user.getRole())
        .issuedAt(Date.from(now))
        .expiration(Date.from(Instant.now().plusSeconds(jwtConfigProperties.getExpirationTime())))
        .signWith(getSigningKey())
        .compact();
  }

  /*
    * Extracts the user ID from the JWT token.
    * @param token the JWT token
   */
  public String extractUserId(String token){
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token){
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Boolean isTokenValid(String token, UUID userId) {
    final String extractedUser = extractUserId(token);
    return extractedUser.equals(userId.toString()) && !isTokenExpired(token);
  }

}

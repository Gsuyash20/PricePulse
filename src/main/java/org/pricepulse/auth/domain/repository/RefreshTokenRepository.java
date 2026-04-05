package org.pricepulse.auth.domain.repository;

import org.pricepulse.auth.domain.entity.RefreshToken;
import org.pricepulse.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
  void revokeAllByUser(@Param("user") User user);
}

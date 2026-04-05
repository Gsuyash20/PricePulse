package org.pricepulse.auth.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoginResponseDTO (
    String accessToken,
    String refreshToken,
    String tokenType,
    Instant expiresIn,
    UUID userId
) {
}

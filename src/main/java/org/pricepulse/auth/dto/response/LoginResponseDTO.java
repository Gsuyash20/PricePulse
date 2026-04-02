package org.pricepulse.auth.dto.response;

import java.time.Instant;

public record LoginResponseDTO (
    String accessToken,
    String refreshToken,
    String tokenType,
    Instant expiresIn
) {
}

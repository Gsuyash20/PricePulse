package org.pricepulse.auth.dto.response;

import java.time.Instant;

public record LoginResponseDTO (
    String accessToken,
    String tokenType,
    Instant expiresIn
) {
}

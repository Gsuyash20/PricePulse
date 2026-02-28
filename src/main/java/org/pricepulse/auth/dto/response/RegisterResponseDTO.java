package org.pricepulse.auth.dto.response;

import java.util.UUID;

public record RegisterResponseDTO(String email, String message, UUID id) {
}

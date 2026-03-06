package org.pricepulse.auth.dto.response;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterResponseDTO(String email, String message, @NotNull UUID id) {
}

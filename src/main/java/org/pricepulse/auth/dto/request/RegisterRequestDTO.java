package org.pricepulse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(@NotBlank String email, @NotBlank String password) {
}

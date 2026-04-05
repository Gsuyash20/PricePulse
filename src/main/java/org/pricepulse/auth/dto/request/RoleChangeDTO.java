package org.pricepulse.auth.dto.request;

public record RoleChangeDTO(
    String userId,
    String role
) {
}

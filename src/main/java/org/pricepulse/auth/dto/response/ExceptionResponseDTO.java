package org.pricepulse.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponseDTO(
    String message,
    @NotNull
    Integer httpStatus,
    @NotNull
    String error,
    String path,
    Instant timestamp,
    Map<String, String> errorContext
) {
}

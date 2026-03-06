package org.pricepulse.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/users")
public interface IAuthController {

  @Operation(
      summary = "Register user",
      description = "Registers a new user with the provided email and password."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successful registration"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  @PostMapping("/register")
  ResponseEntity<@NonNull RegisterResponseDTO> registerUser(@Valid @NotNull @RequestBody RegisterRequestDTO requestDTO);
}

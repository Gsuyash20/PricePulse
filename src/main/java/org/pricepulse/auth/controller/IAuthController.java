package org.pricepulse.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
  @PostMapping("/public/register")
  ResponseEntity<@NonNull RegisterResponseDTO> registerUser(@Valid @NotNull @RequestBody RegisterRequestDTO requestDTO);

  @Operation(
      summary = "Login the user",
      description = "Login a user with the provided email and password."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successful login"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  @PostMapping("/public/login")
  ResponseEntity<@NonNull LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO requestDTO);

  @Operation(
      summary = "refresh the login token",
      description = "Refresh token"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Refreshed token"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  @PostMapping("/refresh-token")
  ResponseEntity<@NonNull LoginResponseDTO> refreshToken(@RequestParam String refreshToken);

  // todo: add logout endpoint
}

package org.pricepulse.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.request.RoleChangeDTO;
import org.pricepulse.auth.dto.response.ProfileResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/users")
public interface IUserController {

  @Operation(
      summary = "Get user profile",
      description = "Fetch the user profile information"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  @GetMapping("/profile")
  ResponseEntity<@NonNull ProfileResponseDTO> fetchUserProfile();

  @Operation(
      summary = "Update the user role",
      description = "Internal api to update the role"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Role updated successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  @PatchMapping("/roles")
  ResponseEntity<@NonNull Void> updateUserRole(@Valid @NotNull @RequestBody RoleChangeDTO requestDTO);


}

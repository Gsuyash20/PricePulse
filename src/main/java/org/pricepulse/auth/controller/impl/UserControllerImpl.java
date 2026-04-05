package org.pricepulse.auth.controller.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.controller.IUserController;
import org.pricepulse.auth.dto.request.RoleChangeDTO;
import org.pricepulse.auth.dto.response.ProfileResponseDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

  private final UserService userService;

  @Override
  public ResponseEntity<@NonNull ProfileResponseDTO> fetchUserProfile() {
    return ResponseEntity.ok(new ProfileResponseDTO("abc@gmail.cm"));
  }

  @Override
  public ResponseEntity<@NonNull Void> updateUserRole(RoleChangeDTO requestDTO) {
    userService.changeUserRole(requestDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}

package org.pricepulse.auth.controller.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.controller.IUserController;
import org.pricepulse.auth.dto.response.ProfileResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {
  @Override
  public ResponseEntity<@NonNull ProfileResponseDTO> fetchUserProfile() {
    return ResponseEntity.ok(new ProfileResponseDTO("abc@gmail.cm"));
  }
}

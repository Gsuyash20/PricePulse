package org.pricepulse.auth.controller.impl;

import lombok.RequiredArgsConstructor;
import org.pricepulse.auth.controller.IAuthController;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements IAuthController {
  private final UserService userService;

  @Override
  public ResponseEntity<RegisterResponseDTO> registerUser(RegisterRequestDTO requestDTO) {
    RegisterResponseDTO responseDTO = userService.createUser(requestDTO);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseDTO);
  }
}

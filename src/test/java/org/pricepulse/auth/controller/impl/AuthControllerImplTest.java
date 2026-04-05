package org.pricepulse.auth.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.constants.MessageConstants;
import org.pricepulse.auth.dto.request.LoginRequestDTO;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.LoginResponseDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.exception.generic.DuplicateResourceException;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.service.AuthService;
import org.pricepulse.auth.service.RefreshTokenService;
import org.pricepulse.auth.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerImplTest {
  @InjectMocks
  private AuthControllerImpl authController;

  @Mock
  private UserService userService;

  @Mock
  private AuthService authService;

  @Mock
  private RefreshTokenService refreshTokenService;

  MockMvc mockMvc;
  RegisterRequestDTO registerRequestDTO;
  RegisterResponseDTO registerResponseDTO;
  LoginRequestDTO loginRequestDTO;
  LoginResponseDTO loginResponseDTO;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

    String email = "abc@gmail.com";
    String password = "123456789";
    UUID id = UUID.randomUUID();
    registerRequestDTO = new RegisterRequestDTO(email, password);
    registerResponseDTO = new RegisterResponseDTO(email, password, id);

    loginRequestDTO = new LoginRequestDTO(email, password);
    loginResponseDTO = new LoginResponseDTO("accessToken", "refreshToken", "BEARER", Instant.now(), UUID.randomUUID());
  }

  public static String asJsonString(final Object obj) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testRegisterUserWhenValidRequestThenReturnResponse() throws Exception {

    when(userService.createUser(any())).thenReturn(registerResponseDTO);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/users/public/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(registerRequestDTO)))
        .andExpect(status().isCreated());

    assertNotNull(registerRequestDTO, "Request is null");
  }

  @Test
  void testRegisterUserWhenEmailAlreadyExistsShouldThrowDuplicateResourceException() {
    when(userService.createUser(any())).thenThrow(new DuplicateResourceException("User with email already exists"));

    assertThrows(
        DuplicateResourceException.class,
        () -> authController.registerUser(registerRequestDTO)
    );

    verify(userService, times(1)).createUser(registerRequestDTO);
  }


  @Test
  void testRegisterUserWhenConcurrentRequestsThenShouldBeHandledProperly() throws Exception {
    when(userService.createUser(any(RegisterRequestDTO.class)))
        .thenReturn(registerResponseDTO);

    // Execute multiple requests in parallel (simulated sequentially here)
    for (int i = 0; i < 5; i++) {
      RegisterRequestDTO request = new RegisterRequestDTO(
          "user" + i + "@example.com",
          "StrongP@ssw0rd123"
      );

      RegisterResponseDTO response = new RegisterResponseDTO(request.email(),
          MessageConstants.USER_CREATED_SUCCESSFULLY, UUID.randomUUID());

      when(userService.createUser(request)).thenReturn(response);

      mockMvc.perform(MockMvcRequestBuilders.post("/users/public/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(request)))
          .andExpect(status().isCreated());
    }

    verify(userService, times(5)).createUser(any(RegisterRequestDTO.class));
  }

  @Test
  void testLoginUserWhenValidRequestThenReturnResponse() throws Exception {
    when(authService.loginUser(any())).thenReturn(loginResponseDTO);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/users/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(loginRequestDTO)))
        .andExpect(status().isOk());

    verify(authService, times(1)).loginUser(loginRequestDTO);
  }

  @Test
  void testRefreshTokenWhenValidTokenThenReturnResponse() throws Exception {
    String refreshToken = "validRefreshToken";

    when(refreshTokenService.refreshToken(refreshToken)).thenReturn(loginResponseDTO);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/users/refresh-token")
            .param("refreshToken", refreshToken))
        .andExpect(status().isOk());

    verify(refreshTokenService, times(1)).refreshToken(refreshToken);
  }

  @Test
  void testLogoutUserWhenValidTokenThenNoContent() throws Exception {
    String refreshToken = "validRefreshToken";

    mockMvc.perform(MockMvcRequestBuilders
            .post("/users/logout")
            .param("refreshToken", refreshToken))
        .andExpect(status().isNoContent());

    verify(refreshTokenService, times(1)).logOutUser(refreshToken);
  }

  @Test
  void testLoginUserWhenInvalidCredentialsThenThrowException() {
    when(authService.loginUser(any())).thenThrow(new InvalidInputException("Invalid credentials"));

    assertThrows(
        InvalidInputException.class,
        () -> authController.loginUser(loginRequestDTO)
    );

    verify(authService, times(1)).loginUser(loginRequestDTO);
  }

  @Test
  void testRefreshTokenWhenInvalidTokenThenThrowException() {
    String refreshToken = "invalidRefreshToken";

    when(refreshTokenService.refreshToken(refreshToken)).thenThrow(new InvalidInputException("Invalid refresh token"));

    assertThrows(
        InvalidInputException.class,
        () -> authController.refreshToken(refreshToken)
    );

    verify(refreshTokenService, times(1)).refreshToken(refreshToken);
  }

  @Test
  void testLogoutUserWhenInvalidTokenThenThrowException() {
    String refreshToken = "invalidRefreshToken";

    doThrow(new InvalidInputException("Invalid refresh token")).when(refreshTokenService).logOutUser(refreshToken);

    assertThrows(
        InvalidInputException.class,
        () -> authController.logoutUser(refreshToken)
    );

    verify(refreshTokenService, times(1)).logOutUser(refreshToken);
  }
}
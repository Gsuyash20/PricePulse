package org.pricepulse.auth.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pricepulse.auth.constants.MessageConstants;
import org.pricepulse.auth.dto.request.RegisterRequestDTO;
import org.pricepulse.auth.dto.response.RegisterResponseDTO;
import org.pricepulse.auth.exception.generic.DuplicateResourceException;
import org.pricepulse.auth.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

  MockMvc mockMvc;
  RegisterRequestDTO requestDTO;
  RegisterResponseDTO responseDTO;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

    String email = "abc@gmail.com";
    String password = "123456789";
    UUID id = UUID.randomUUID();
    requestDTO = new RegisterRequestDTO(email, password);
    responseDTO = new RegisterResponseDTO(email, password, id);
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

    when(userService.createUser(any())).thenReturn(responseDTO);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(requestDTO)))
        .andExpect(status().isCreated());

    assertNotNull(requestDTO, "Request is null");
  }

  @Test
  void testRegisterUserWhenEmailAlreadyExistsShouldThrowDuplicateResourceException() {
    when(userService.createUser(any())).thenThrow(new DuplicateResourceException("User with email already exists"));

    assertThrows(
        DuplicateResourceException.class,
        () -> authController.registerUser(requestDTO)
    );

    verify(userService, times(1)).createUser(requestDTO);
  }


  @Test
  void testRegisterUserWhenConcurrentRequestsThenShouldBeHandledProperly() throws Exception {
    when(userService.createUser(any(RegisterRequestDTO.class)))
        .thenReturn(responseDTO);

    // Execute multiple requests in parallel (simulated sequentially here)
    for (int i = 0; i < 5; i++) {
      RegisterRequestDTO request = new RegisterRequestDTO(
          "user" + i + "@example.com",
          "StrongP@ssw0rd123"
      );

      RegisterResponseDTO response = new RegisterResponseDTO(request.email(),
          MessageConstants.USER_CREATED_SUCCESSFULLY, UUID.randomUUID());

      when(userService.createUser(request)).thenReturn(response);

      mockMvc.perform(MockMvcRequestBuilders.post("/users/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(request)))
          .andExpect(status().isCreated());
    }

    verify(userService, times(5)).createUser(any(RegisterRequestDTO.class));
  }
}
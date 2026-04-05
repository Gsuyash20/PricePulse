package org.pricepulse.auth.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerImplTest {
  @InjectMocks
  private UserControllerImpl userController;

  MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  void testFetchUserProfileShouldReturnProfileResponse() throws Exception {
    mockMvc.perform(get("/users/profile"))
        .andExpect(status().isOk());
  }
}

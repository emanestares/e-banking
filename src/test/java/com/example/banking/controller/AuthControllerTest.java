package com.example.banking.controller;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCallLoginEndpoint() throws Exception {

        // -------- REQUEST --------
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        // -------- RESPONSE --------
        LoginResponse response = new LoginResponse(
                "fake-jwt",
                "john",
                "John Doe",
                List.of("ROLE_USER"),   // ✅ FIXED (List, not Set)
                1L,
                "ACC123"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        // -------- ASSERT --------
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("fake-jwt"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));
    }
}
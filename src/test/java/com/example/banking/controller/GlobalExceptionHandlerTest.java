package com.example.banking.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // =====================================================
    // 500 - RuntimeException (your "validation" endpoint)
    // =====================================================
    @Test
    void shouldHandleRuntimeExceptionAsGenericError() throws Exception {

        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("An internal error occurred. Please try again."));
    }

    // =====================================================
    // 401 - BadCredentialsException
    // =====================================================
    @Test
    void shouldHandleBadCredentials() throws Exception {

        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"));
    }

    // =====================================================
    // 403 - AccessDeniedException
    // =====================================================
    @Test
    void shouldHandleAccessDenied() throws Exception {

        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Access denied: no access"));
    }

    // =====================================================
    // 400 - IllegalStateException
    // =====================================================
    @Test
    void shouldHandleIllegalState() throws Exception {

        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("insufficient funds"));
    }

    // =====================================================
    // 400 - IllegalArgumentException
    // =====================================================
    @Test
    void shouldHandleIllegalArgument() throws Exception {

        mockMvc.perform(get("/test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("invalid request"));
    }

    // =====================================================
    // 500 - RuntimeException (runtime endpoint)
    // =====================================================
    @Test
    void shouldHandleRuntimeException() throws Exception {

        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // 500 - Generic Exception
    // =====================================================
    @Test
    void shouldHandleGenericException() throws Exception {

        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("An internal error occurred. Please try again."));
    }
}
package com.example.banking.controller;

import com.example.banking.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------- TEST CONTROLLER ----------------
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/validation")
        public void validation() {
            throw new RuntimeException("Validation failed");
        }

        @GetMapping("/bad-credentials")
        public void badCredentials() {
            throw new BadCredentialsException("bad login");
        }

        @GetMapping("/access-denied")
        public void accessDenied() {
            throw new org.springframework.security.access.AccessDeniedException("no access");
        }

        @GetMapping("/illegal-state")
        public void illegalState() {
            throw new IllegalStateException("insufficient funds");
        }

        @GetMapping("/illegal-arg")
        public void illegalArg() {
            throw new IllegalArgumentException("invalid request");
        }

        @GetMapping("/runtime")
        public void runtime() {
            throw new RuntimeException("not found");
        }

        @GetMapping("/generic")
        public void generic() throws Exception {
            throw new Exception("system crash");
        }
    }

    // ---------------- TEST CASES ----------------

    @Test
    void shouldHandleBadCredentials() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void shouldHandleAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: no access"));
    }

    @Test
    void shouldHandleIllegalState() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("insufficient funds"));
    }

    @Test
    void shouldHandleIllegalArgument() throws Exception {
        mockMvc.perform(get("/test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("invalid request"));
    }

    @Test
    void shouldHandleRuntimeException() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("not found"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("An internal error occurred. Please try again."));
    }
}
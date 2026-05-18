package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void shouldCreateUsingAllArgsConstructor() {
        LoginResponse response = new LoginResponse(
                "token123",
                "Bearer",
                "john",
                "John Doe",
                List.of("ROLE_USER"),
                1L,
                "ACC123"
        );

        assertEquals("token123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john", response.getUsername());
        assertEquals("John Doe", response.getFullName());
        assertEquals(List.of("ROLE_USER"), response.getRoles());
        assertEquals(1L, response.getAccountId());
        assertEquals("ACC123", response.getAccountNumber());
    }

    @Test
    void shouldCreateUsingCustomConstructor_andForceBearerTokenType() {
        LoginResponse response = new LoginResponse(
                "token456",
                "john",
                "John Doe",
                List.of("ROLE_ADMIN"),
                2L,
                "ACC999"
        );

        assertEquals("token456", response.getToken());
        assertEquals("Bearer", response.getTokenType()); // enforced default
        assertEquals("john", response.getUsername());
        assertEquals("John Doe", response.getFullName());
        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());
        assertEquals(2L, response.getAccountId());
        assertEquals("ACC999", response.getAccountNumber());
    }

    @Test
    void shouldAllowSetterModification() {
        LoginResponse response = new LoginResponse(
                "token",
                "Bearer",
                "john",
                "John Doe",
                List.of("ROLE_USER"),
                null,
                null
        );

        response.setToken("new-token");
        response.setTokenType("JWT");
        response.setAccountId(100L);

        assertEquals("new-token", response.getToken());
        assertEquals("JWT", response.getTokenType());
        assertEquals(100L, response.getAccountId());
    }

    @Test
    void shouldHandleNullValues() {
        LoginResponse response = new LoginResponse(
                "token",
                "john",
                "John Doe",
                null,
                null,
                null
        );

        assertNull(response.getRoles());
        assertNull(response.getAccountId());
        assertNull(response.getAccountNumber());
    }
}
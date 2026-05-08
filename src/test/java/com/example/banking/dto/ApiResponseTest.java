package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void shouldCreateResponseUsingAllArgsConstructor() {

        ApiResponse<String> response =
                new ApiResponse<>(true, "Success", "data");

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("data", response.getData());
    }

    @Test
    void shouldCreateResponseUsingNoArgsConstructorAndSetters() {

        ApiResponse<String> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage("OK");
        response.setData("payload");

        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    void shouldCreateSuccessResponseWithDefaultMessage() {

        ApiResponse<String> response =
                ApiResponse.ok("payload");

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    void shouldCreateSuccessResponseWithCustomMessage() {

        ApiResponse<String> response =
                ApiResponse.ok("Custom message", "payload");

        assertTrue(response.isSuccess());
        assertEquals("Custom message", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    void shouldCreateErrorResponse() {

        ApiResponse<String> response =
                ApiResponse.error("Something went wrong");

        assertFalse(response.isSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void shouldSupportGenericTypesCorrectly() {

        ApiResponse<Integer> response =
                ApiResponse.ok(123);

        assertTrue(response.isSuccess());
        assertEquals(123, response.getData());
    }
}
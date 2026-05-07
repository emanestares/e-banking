package com.example.banking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidation_whenValidRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_whenUsernameIsBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("secret");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void shouldFailValidation_whenPasswordIsBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void shouldFailValidation_whenBothFieldsBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertEquals(2, violations.size());
    }
}
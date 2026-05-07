package com.example.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;


    @NotNull(message = "Initial deposit is required and not negative")
    @DecimalMax(value = "10000000", message = "Initial deposit must not exceed 10 million")
    @DecimalMin(value = "0.01", message = "Initial deposit must be greater than 0")
    private BigDecimal initialDeposit;
}

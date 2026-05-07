package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollRequest {

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVINGS or CHECKING

    @NotBlank(message = "Purpose of account is required")
    private String purpose;

    @NotNull(message = "Initial deposit is required and not negative")
    @DecimalMax(value = "10000000", message = "Initial deposit must not exceed 10 million")
    @DecimalMin(value = "0.00", message = "Deposit cannot be negative")
    private BigDecimal initialDeposit;
}

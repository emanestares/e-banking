package com.example.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollRequest {

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVINGS or CHECKING

    @NotBlank(message = "Purpose of account is required")
    private String purpose;

    @NotNull(message = "Initial deposit is required and not negative")
    @Positive(message = "Amount must be a positive value")
    @DecimalMin(value = "0.00", message = "Deposit cannot be negative")
    private BigDecimal initialDeposit;
}

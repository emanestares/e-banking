package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollRequest {

    @NotBlank(message = "Account type is required")
    private String accountType;   // SAVINGS or CHECKING

    private BigDecimal initialDeposit;
}

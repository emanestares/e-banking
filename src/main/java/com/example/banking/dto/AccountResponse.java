package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String ownerFullName;
    private String ownerUsername;
    private Long ownerId;
}
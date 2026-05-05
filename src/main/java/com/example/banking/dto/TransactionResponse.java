package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String referenceNumber;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String direction; // DEBIT or CREDIT relative to the requesting account
}
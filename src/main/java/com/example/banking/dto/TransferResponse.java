package com.example.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private String referenceNumber;
    private String status;
    private BigDecimal amount;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String description;
    private LocalDateTime transactionDate;
    private BigDecimal newSenderBalance;
}
package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransferResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        TransferResponse response = new TransferResponse();

        LocalDateTime now = LocalDateTime.now();

        response.setReferenceNumber("TXN-123");
        response.setStatus("COMPLETED");
        response.setAmount(new BigDecimal("250.75"));
        response.setSenderAccountNumber("ACC1");
        response.setReceiverAccountNumber("ACC2");
        response.setDescription("Test transfer");
        response.setTransactionDate(now);
        response.setNewSenderBalance(new BigDecimal("750.25"));

        assertEquals("TXN-123", response.getReferenceNumber());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("250.75"), response.getAmount());
        assertEquals("ACC1", response.getSenderAccountNumber());
        assertEquals("ACC2", response.getReceiverAccountNumber());
        assertEquals("Test transfer", response.getDescription());
        assertEquals(now, response.getTransactionDate());
        assertEquals(new BigDecimal("750.25"), response.getNewSenderBalance());
    }

    @Test
    void shouldHandleNullValuesByDefault() {
        TransferResponse response = new TransferResponse();

        assertNull(response.getReferenceNumber());
        assertNull(response.getStatus());
        assertNull(response.getAmount());
        assertNull(response.getSenderAccountNumber());
        assertNull(response.getReceiverAccountNumber());
        assertNull(response.getDescription());
        assertNull(response.getTransactionDate());
        assertNull(response.getNewSenderBalance());
    }

    @Test
    void shouldPreserveBigDecimalPrecision() {
        TransferResponse response = new TransferResponse();

        BigDecimal amount = new BigDecimal("1000.99");
        BigDecimal balance = new BigDecimal("5000.123");

        response.setAmount(amount);
        response.setNewSenderBalance(balance);

        assertEquals(amount, response.getAmount());
        assertEquals(balance, response.getNewSenderBalance());
    }

    @Test
    void shouldAllowStatusValues() {
        TransferResponse response = new TransferResponse();

        response.setStatus("COMPLETED");
        assertEquals("COMPLETED", response.getStatus());

        response.setStatus("FAILED");
        assertEquals("FAILED", response.getStatus());
    }
}
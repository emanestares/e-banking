package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        TransactionResponse response = new TransactionResponse();

        LocalDateTime now = LocalDateTime.now();

        response.setId(1L);
        response.setReferenceNumber("REF123");
        response.setAmount(new BigDecimal("250.75"));
        response.setTransactionType("TRANSFER");
        response.setStatus("COMPLETED");
        response.setDescription("Test transaction");
        response.setCreatedAt(now);
        response.setSenderAccountNumber("ACC1");
        response.setReceiverAccountNumber("ACC2");
        response.setDirection("DEBIT");

        assertEquals(1L, response.getId());
        assertEquals("REF123", response.getReferenceNumber());
        assertEquals(new BigDecimal("250.75"), response.getAmount());
        assertEquals("TRANSFER", response.getTransactionType());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("Test transaction", response.getDescription());
        assertEquals(now, response.getCreatedAt());
        assertEquals("ACC1", response.getSenderAccountNumber());
        assertEquals("ACC2", response.getReceiverAccountNumber());
        assertEquals("DEBIT", response.getDirection());
    }

    @Test
    void shouldAllowNullValuesByDefault() {
        TransactionResponse response = new TransactionResponse();

        assertNull(response.getId());
        assertNull(response.getReferenceNumber());
        assertNull(response.getAmount());
        assertNull(response.getTransactionType());
        assertNull(response.getStatus());
        assertNull(response.getDescription());
        assertNull(response.getCreatedAt());
        assertNull(response.getSenderAccountNumber());
        assertNull(response.getReceiverAccountNumber());
        assertNull(response.getDirection());
    }

    @Test
    void shouldHandleCreditDirectionValue() {
        TransactionResponse response = new TransactionResponse();
        response.setDirection("CREDIT");

        assertEquals("CREDIT", response.getDirection());
    }

    @Test
    void shouldHandleDebitDirectionValue() {
        TransactionResponse response = new TransactionResponse();
        response.setDirection("DEBIT");

        assertEquals("DEBIT", response.getDirection());
    }

    @Test
    void shouldSupportBigDecimalPrecision() {
        TransactionResponse response = new TransactionResponse();

        BigDecimal amount = new BigDecimal("1000.99");
        response.setAmount(amount);

        assertEquals(amount, response.getAmount());
    }
}
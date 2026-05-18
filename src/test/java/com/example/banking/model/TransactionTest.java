package com.example.banking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldBuildTransactionWithDefaults() {

        Transaction transaction = Transaction.builder()
                .referenceNumber("REF123")
                .amount(new BigDecimal("100.00"))
                .transactionType("TRANSFER")
                .build();

        assertNotNull(transaction);

        assertEquals("REF123", transaction.getReferenceNumber());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals("TRANSFER", transaction.getTransactionType());

        // default value from builder
        assertEquals("COMPLETED", transaction.getStatus());

        // relations not set in builder
        assertNull(transaction.getSenderAccount());
        assertNull(transaction.getReceiverAccount());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {

        Transaction transaction = new Transaction();

        transaction.setId(1L);
        transaction.setReferenceNumber("REF999");
        transaction.setAmount(new BigDecimal("250.50"));
        transaction.setTransactionType("DEPOSIT");
        transaction.setStatus("PENDING");
        transaction.setDescription("Test transaction");

        assertEquals(1L, transaction.getId());
        assertEquals("REF999", transaction.getReferenceNumber());
        assertEquals(new BigDecimal("250.50"), transaction.getAmount());
        assertEquals("DEPOSIT", transaction.getTransactionType());
        assertEquals("PENDING", transaction.getStatus());
        assertEquals("Test transaction", transaction.getDescription());
    }

    @Test
    void shouldInitializeCreatedAtOnPrePersist() {

        Transaction transaction = new Transaction();

        transaction.onCreate();

        assertNotNull(transaction.getCreatedAt());
        assertTrue(transaction.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void shouldAllowCustomStatusInBuilder() {

        Transaction transaction = Transaction.builder()
                .referenceNumber("REF777")
                .amount(new BigDecimal("50.00"))
                .transactionType("WITHDRAWAL")
                .status("FAILED")
                .build();

        assertEquals("FAILED", transaction.getStatus());
    }

    @Test
    void shouldCreateTransactionUsingAllArgsConstructor() {

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction(
                10L,
                "REF555",
                null,
                null,
                new BigDecimal("75.00"),
                "TRANSFER",
                "COMPLETED",
                "Sample",
                now
        );

        assertEquals(10L, transaction.getId());
        assertEquals("REF555", transaction.getReferenceNumber());
        assertEquals(new BigDecimal("75.00"), transaction.getAmount());
        assertEquals("TRANSFER", transaction.getTransactionType());
        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("Sample", transaction.getDescription());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void shouldAllowNullDescription() {

        Transaction transaction = Transaction.builder()
                .referenceNumber("REF000")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .build();

        assertNull(transaction.getDescription());
    }
}
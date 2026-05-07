package com.example.banking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldBuildAccountWithDefaults() {
        Account account = Account.builder()
                .accountNumber("1234567890")
                .build();

        assertNotNull(account);
        assertEquals("1234567890", account.getAccountNumber());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("SAVINGS", account.getAccountType());
        assertTrue(account.getIsActive());
        assertNotNull(account.getSentTransactions());
        assertNotNull(account.getReceivedTransactions());
        assertTrue(account.getSentTransactions().isEmpty());
        assertTrue(account.getReceivedTransactions().isEmpty());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        Account account = new Account();

        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("100.50"));
        account.setAccountType("CURRENT");
        account.setIsActive(false);

        assertEquals(1L, account.getId());
        assertEquals("ACC123", account.getAccountNumber());
        assertEquals(new BigDecimal("100.50"), account.getBalance());
        assertEquals("CURRENT", account.getAccountType());
        assertFalse(account.getIsActive());
    }

    @Test
    void shouldInitializeTimestampsOnPrePersist() {
        Account account = new Account();

        account.onCreate();

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() throws InterruptedException {
        Account account = new Account();

        account.onCreate();
        LocalDateTime createdAt = account.getCreatedAt();
        LocalDateTime firstUpdatedAt = account.getUpdatedAt();

        // ensure time difference
        Thread.sleep(5);

        account.onUpdate();

        assertEquals(createdAt, account.getCreatedAt());
        assertTrue(account.getUpdatedAt().isAfter(firstUpdatedAt));
    }

    @Test
    void shouldAllowCustomValuesInBuilder() {
        LocalDateTime now = LocalDateTime.now();

        Account account = Account.builder()
                .accountNumber("999")
                .balance(new BigDecimal("500.00"))
                .accountType("CURRENT")
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("999", account.getAccountNumber());
        assertEquals(new BigDecimal("500.00"), account.getBalance());
        assertEquals("CURRENT", account.getAccountType());
        assertFalse(account.getIsActive());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
    }
}

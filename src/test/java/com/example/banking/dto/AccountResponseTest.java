package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountResponseTest {

    @Test
    void shouldSetAndGetAllFields() {
        AccountResponse response = new AccountResponse();

        LocalDateTime now = LocalDateTime.now();

        response.setId(1L);
        response.setAccountNumber("ACC123");
        response.setBalance(new BigDecimal("1000.50"));
        response.setAccountType("SAVINGS");
        response.setIsActive(true);
        response.setCreatedAt(now);
        response.setOwnerFullName("John Doe");
        response.setOwnerUsername("john");
        response.setOwnerId(99L);

        assertEquals(1L, response.getId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals(new BigDecimal("1000.50"), response.getBalance());
        assertEquals("SAVINGS", response.getAccountType());
        assertTrue(response.getIsActive());
        assertEquals(now, response.getCreatedAt());
        assertEquals("John Doe", response.getOwnerFullName());
        assertEquals("john", response.getOwnerUsername());
        assertEquals(99L, response.getOwnerId());
    }

    @Test
    void shouldSupportAllArgsConstruction() {
        LocalDateTime now = LocalDateTime.now();

        AccountResponse response = new AccountResponse();

        response.setId(2L);
        response.setAccountNumber("ACC999");
        response.setBalance(BigDecimal.TEN);
        response.setAccountType("CURRENT");
        response.setIsActive(false);
        response.setCreatedAt(now);
        response.setOwnerFullName("Jane Doe");
        response.setOwnerUsername("jane");
        response.setOwnerId(100L);

        assertNotNull(response);
        assertEquals("ACC999", response.getAccountNumber());
        assertEquals("Jane Doe", response.getOwnerFullName());
    }

    @Test
    void shouldHandleNullValues() {
        AccountResponse response = new AccountResponse();

        assertNull(response.getId());
        assertNull(response.getAccountNumber());
        assertNull(response.getBalance());
        assertNull(response.getAccountType());
        assertNull(response.getIsActive());
        assertNull(response.getCreatedAt());
        assertNull(response.getOwnerFullName());
        assertNull(response.getOwnerUsername());
        assertNull(response.getOwnerId());
    }
}
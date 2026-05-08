package com.example.banking.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountResponseTest {

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

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

    // =====================================================
    // NULL DEFAULTS
    // =====================================================

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

    // =====================================================
    // EQUALS & HASHCODE
    // =====================================================

    @Test
    void shouldSupportEqualsAndHashCode() {

        LocalDateTime now = LocalDateTime.now();

        AccountResponse r1 = new AccountResponse();
        r1.setId(1L);
        r1.setAccountNumber("ACC1");
        r1.setBalance(BigDecimal.TEN);
        r1.setAccountType("SAVINGS");
        r1.setIsActive(true);
        r1.setCreatedAt(now);
        r1.setOwnerFullName("John Doe");
        r1.setOwnerUsername("john");
        r1.setOwnerId(100L);

        AccountResponse r2 = new AccountResponse();
        r2.setId(1L);
        r2.setAccountNumber("ACC1");
        r2.setBalance(BigDecimal.TEN);
        r2.setAccountType("SAVINGS");
        r2.setIsActive(true);
        r2.setCreatedAt(now);
        r2.setOwnerFullName("John Doe");
        r2.setOwnerUsername("john");
        r2.setOwnerId(100L);

        assertEquals(r1, r2);

        assertEquals(r1.hashCode(), r2.hashCode());
    }

    // =====================================================
    // TO STRING
    // =====================================================

    @Test
    void shouldGenerateToString() {

        AccountResponse response = new AccountResponse();

        response.setId(1L);
        response.setAccountNumber("ACC123");

        String text = response.toString();

        assertNotNull(text);

        assertTrue(text.contains("ACC123"));

        assertTrue(text.contains("id=1"));
    }

    // =====================================================
    // NOT EQUAL
    // =====================================================

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {

        AccountResponse r1 = new AccountResponse();
        r1.setId(1L);

        AccountResponse r2 = new AccountResponse();
        r2.setId(2L);

        assertNotEquals(r1, r2);
    }
}
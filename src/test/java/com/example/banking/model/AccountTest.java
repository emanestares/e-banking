package com.example.banking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    // =====================================================
    // BUILDER DEFAULTS
    // =====================================================

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

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    @Test
    void shouldSetAndGetFieldsCorrectly() {

        User user = User.builder()
                .id(1L)
                .username("john")
                .build();

        Account account = new Account();

        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("100.50"));
        account.setAccountType("CURRENT");
        account.setIsActive(false);
        account.setUser(user);

        assertEquals(1L, account.getId());

        assertEquals("ACC123", account.getAccountNumber());

        assertEquals(new BigDecimal("100.50"), account.getBalance());

        assertEquals("CURRENT", account.getAccountType());

        assertFalse(account.getIsActive());

        assertEquals(user, account.getUser());
    }

    // =====================================================
    // PRE PERSIST
    // =====================================================

    @Test
    void shouldInitializeTimestampsOnPrePersist() {


        Account account = new Account();

        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());

        account.onCreate();

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        assertTrue(
                Math.abs(java.time.Duration.between(
                        account.getCreatedAt(),
                        account.getUpdatedAt()
                ).toMillis()) < 100
        );
    }

    // =====================================================
    // PRE UPDATE
    // =====================================================

    @Test
    void shouldUpdateTimestampOnPreUpdate() {

        Account account = new Account();

        account.onCreate();

        LocalDateTime createdAt = account.getCreatedAt();

        LocalDateTime oldUpdatedAt =
                account.getUpdatedAt().minusSeconds(1);

        account.setUpdatedAt(oldUpdatedAt);

        account.onUpdate();

        assertEquals(createdAt, account.getCreatedAt());

        assertTrue(
                account.getUpdatedAt().isAfter(oldUpdatedAt)
        );
    }

    // =====================================================
    // CUSTOM BUILDER VALUES
    // =====================================================

    @Test
    void shouldAllowCustomValuesInBuilder() {

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(5L)
                .username("alice")
                .build();

        Account account = Account.builder()
                .accountNumber("999")
                .balance(new BigDecimal("500.00"))
                .accountType("CURRENT")
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .user(user)
                .build();

        assertEquals("999", account.getAccountNumber());

        assertEquals(new BigDecimal("500.00"), account.getBalance());

        assertEquals("CURRENT", account.getAccountType());

        assertFalse(account.getIsActive());

        assertEquals(now, account.getCreatedAt());

        assertEquals(now, account.getUpdatedAt());

        assertEquals(user, account.getUser());
    }

    // =====================================================
    // ALL ARGS CONSTRUCTOR
    // =====================================================

    @Test
    void shouldCreateAccountUsingAllArgsConstructor() {

        User user = User.builder()
                .id(1L)
                .username("john")
                .build();

        LocalDateTime now = LocalDateTime.now();

        List<Transaction> sent = List.of();
        List<Transaction> received = List.of();

        Account account = new Account(
                1L,
                "ACC100",
                user,
                BigDecimal.TEN,
                "SAVINGS",
                true,
                now,
                now,
                sent,
                received
        );

        assertEquals(1L, account.getId());

        assertEquals("ACC100", account.getAccountNumber());

        assertEquals(user, account.getUser());

        assertEquals(BigDecimal.TEN, account.getBalance());

        assertEquals("SAVINGS", account.getAccountType());

        assertTrue(account.getIsActive());

        assertEquals(now, account.getCreatedAt());

        assertEquals(now, account.getUpdatedAt());

        assertEquals(sent, account.getSentTransactions());

        assertEquals(received, account.getReceivedTransactions());
    }

    // =====================================================
    // TRANSACTION COLLECTIONS
    // =====================================================

    @Test
    void shouldAllowAddingTransactionsToCollections() {

        Account account = Account.builder()
                .accountNumber("ACC1")
                .build();

        Transaction sentTx = Transaction.builder()
                .id(1L)
                .build();

        Transaction receivedTx = Transaction.builder()
                .id(2L)
                .build();

        account.getSentTransactions().add(sentTx);

        account.getReceivedTransactions().add(receivedTx);

        assertEquals(1, account.getSentTransactions().size());

        assertEquals(1, account.getReceivedTransactions().size());

        assertEquals(sentTx, account.getSentTransactions().get(0));

        assertEquals(receivedTx, account.getReceivedTransactions().get(0));
    }
}
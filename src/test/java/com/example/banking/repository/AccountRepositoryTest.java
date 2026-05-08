package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    // =====================================================
    // HELPERS
    // =====================================================

    private User createUser(String username) {

        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .passwordHash("password")
                .fullName(username)
                .isActive(true)
                .build();

        return entityManager.persistAndFlush(user);
    }

    private Account createAccount(
            User user,
            String accountNumber,
            boolean isActive
    ) {

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(BigDecimal.valueOf(100))
                .accountType("SAVINGS")
                .isActive(isActive)
                .build();

        return entityManager.persistAndFlush(account);
    }

    // =====================================================
    // FIND BY USER ID
    // =====================================================

    @Test
    @DisplayName("findByUserId should return accounts for a user")
    void shouldFindByUserId() {

        User user = createUser("john");

        createAccount(user, "ACC1", true);
        createAccount(user, "ACC2", true);

        List<Account> result =
                accountRepository.findByUserId(user.getId());

        assertEquals(2, result.size());
    }

    // =====================================================
    // FIND BY ACCOUNT NUMBER
    // =====================================================

    @Test
    @DisplayName("findByAccountNumber should return correct account")
    void shouldFindByAccountNumber() {

        User user = createUser("john");

        createAccount(user, "ACC123", true);

        Optional<Account> result =
                accountRepository.findByAccountNumber("ACC123");

        assertTrue(result.isPresent());

        assertEquals(
                "ACC123",
                result.get().getAccountNumber()
        );
    }

    @Test
    @DisplayName("findByAccountNumber should return empty if missing")
    void shouldReturnEmptyWhenAccountNumberMissing() {

        Optional<Account> result =
                accountRepository.findByAccountNumber("MISSING");

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // ACTIVE ACCOUNT LOOKUP
    // =====================================================

    @Test
    @DisplayName("findByAccountNumberAndIsActiveTrue should return only active account")
    void shouldFindActiveAccountByAccountNumber() {

        User user = createUser("john");

        createAccount(user, "ACC123", false);

        Optional<Account> result =
                accountRepository.findByAccountNumberAndIsActiveTrue("ACC123");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByAccountNumberAndIsActiveTrue should return active account")
    void shouldReturnActiveAccount() {

        User user = createUser("john");

        createAccount(user, "ACC123", true);

        Optional<Account> result =
                accountRepository.findByAccountNumberAndIsActiveTrue("ACC123");

        assertTrue(result.isPresent());

        assertTrue(result.get().getIsActive());
    }

    // =====================================================
    // EXISTS
    // =====================================================

    @Test
    @DisplayName("existsByAccountNumber should return true if exists")
    void shouldCheckIfAccountExists() {

        User user = createUser("john");

        createAccount(user, "ACC999", true);

        boolean exists =
                accountRepository.existsByAccountNumber("ACC999");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByAccountNumber should return false if missing")
    void shouldReturnFalseWhenAccountMissing() {

        boolean exists =
                accountRepository.existsByAccountNumber("UNKNOWN");

        assertFalse(exists);
    }

    // =====================================================
    // FIND ALL ACTIVE
    // =====================================================

    @Test
    @DisplayName("findAllActiveAccountsWithUsers should return only active accounts")
    void shouldFindAllActiveAccountsWithUsers() {

        User user = createUser("john");

        createAccount(user, "ACC1", true);
        createAccount(user, "ACC2", true);
        createAccount(user, "ACC3", false);

        List<Account> result =
                accountRepository.findAllActiveAccountsWithUsers();

        assertEquals(2, result.size());

        assertTrue(
                result.stream().allMatch(Account::getIsActive)
        );

        assertNotNull(result.get(0).getUser());
    }

    // =====================================================
    // FIND ACTIVE BY USER ID
    // =====================================================

    @Test
    @DisplayName("findActiveAccountsByUserId should return only active accounts")
    void shouldFindActiveAccountsByUserId() {

        User user = createUser("john");

        createAccount(user, "ACC1", true);
        createAccount(user, "ACC2", false);

        List<Account> result =
                accountRepository.findActiveAccountsByUserId(user.getId());

        assertEquals(1, result.size());

        assertTrue(result.get(0).getIsActive());

        assertEquals("ACC1", result.get(0).getAccountNumber());
    }

    @Test
    @DisplayName("findActiveAccountsByUserId should return empty when no active accounts")
    void shouldReturnEmptyWhenNoActiveAccounts() {

        User user = createUser("john");

        createAccount(user, "ACC1", false);

        List<Account> result =
                accountRepository.findActiveAccountsByUserId(user.getId());

        assertTrue(result.isEmpty());
    }
}
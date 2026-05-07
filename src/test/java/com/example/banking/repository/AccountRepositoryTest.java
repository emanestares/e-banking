package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
    private EntityManager entityManager;

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPasswordHash("password");
        user.setFullName(username);

        entityManager.persist(user);
        return user;
    }

    private Account createAccount(User user, String accountNumber, boolean isActive) {
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(BigDecimal.valueOf(100))
                .isActive(isActive)
                .build();

        entityManager.persist(account);
        return account;
    }

    @Test
    @DisplayName("findByUserId should return accounts for a user")
    void shouldFindByUserId() {
        User user = createUser("john");
        createAccount(user, "ACC1", true);
        createAccount(user, "ACC2", true);

        List<Account> result = accountRepository.findByUserId(user.getId());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByAccountNumber should return correct account")
    void shouldFindByAccountNumber() {
        User user = createUser("john");
        createAccount(user, "ACC123", true);

        Optional<Account> result = accountRepository.findByAccountNumber("ACC123");

        assertTrue(result.isPresent());
        assertEquals("ACC123", result.get().getAccountNumber());
    }

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
    @DisplayName("existsByAccountNumber should return true if exists")
    void shouldCheckIfAccountExists() {
        User user = createUser("john");
        createAccount(user, "ACC999", true);

        boolean exists = accountRepository.existsByAccountNumber("ACC999");

        assertTrue(exists);
    }

    @Test
    @DisplayName("findAllActiveAccountsWithUsers should return only active accounts ordered")
    void shouldFindAllActiveAccountsWithUsers() {
        User user = createUser("john");

        createAccount(user, "ACC1", true);
        createAccount(user, "ACC2", true);
        createAccount(user, "ACC3", false);

        List<Account> result = accountRepository.findAllActiveAccountsWithUsers();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Account::getIsActive));

        assertTrue(
                result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt())
                        || result.get(0).getCreatedAt().isEqual(result.get(1).getCreatedAt())
        );
    }

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
    }
}
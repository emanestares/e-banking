package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    // =====================================================
    // HELPERS
    // =====================================================

    private User createUser(String username) {

        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .passwordHash("pass")
                .fullName(username)
                .isActive(true)
                .build();

        return entityManager.persistAndFlush(user);
    }

    private Account createAccount(User user, String accNo) {

        Account account = Account.builder()
                .accountNumber(accNo)
                .user(user)
                .balance(BigDecimal.valueOf(100))
                .accountType("SAVINGS")
                .isActive(true)
                .build();

        return entityManager.persistAndFlush(account);
    }

    private Transaction createTransaction(
            Account sender,
            Account receiver,
            String reference
    ) {

        Transaction tx = Transaction.builder()
                .referenceNumber(reference)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(BigDecimal.valueOf(100))
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description("Test transfer")
                .build();

        return entityManager.persistAndFlush(tx);
    }

    // =====================================================
    // FIND BY ACCOUNT ID
    // =====================================================

    @Test
    @DisplayName("findAllByAccountId should return sender and receiver transactions")
    void shouldFindTransactionsByAccountId() {

        User user = createUser("john");

        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTransaction(acc1, acc2, "TX1");
        createTransaction(acc2, acc1, "TX2");

        List<Transaction> result =
                transactionRepository.findAllByAccountId(acc1.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream().anyMatch(t -> t.getReferenceNumber().equals("TX1"))
        );

        assertTrue(
                result.stream().anyMatch(t -> t.getReferenceNumber().equals("TX2"))
        );
    }

    @Test
    @DisplayName("findAllByAccountId should return empty when account has no transactions")
    void shouldReturnEmptyWhenNoTransactionsExist() {

        User user = createUser("john");

        Account acc = createAccount(user, "A1");

        List<Transaction> result =
                transactionRepository.findAllByAccountId(acc.getId());

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // PAGED RESULTS
    // =====================================================

    @Test
    @DisplayName("findAllByAccountIdPaged should return paginated results")
    void shouldReturnPagedTransactions() {

        User user = createUser("john");

        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTransaction(acc1, acc2, "TX1");
        createTransaction(acc1, acc2, "TX2");
        createTransaction(acc1, acc2, "TX3");

        Pageable pageable = PageRequest.of(0, 2);

        Page<Transaction> page =
                transactionRepository.findAllByAccountIdPaged(
                        acc1.getId(),
                        pageable
                );

        assertEquals(2, page.getContent().size());

        assertEquals(3, page.getTotalElements());

        assertEquals(2, page.getTotalPages());
    }

    // =====================================================
    // FIND ALL
    // =====================================================

    @Test
    @DisplayName("findAllWithDetails should return all transactions")
    void shouldFindAllWithDetails() {

        User user = createUser("john");

        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTransaction(acc1, acc2, "TX1");
        createTransaction(acc1, acc2, "TX2");

        List<Transaction> result =
                transactionRepository.findAllWithDetails();

        assertEquals(2, result.size());

        assertNotNull(result.get(0).getSenderAccount());

        assertNotNull(result.get(0).getReceiverAccount());
    }

    @Test
    @DisplayName("findAllWithDetails should return empty when no transactions exist")
    void shouldReturnEmptyWhenNoTransactionsExistForFindAll() {

        List<Transaction> result =
                transactionRepository.findAllWithDetails();

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // FIND BY USER ID
    // =====================================================

    @Test
    @DisplayName("findAllByUserId should return user transactions")
    void shouldFindTransactionsByUserId() {

        User user = createUser("john");

        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTransaction(acc1, acc2, "TX1");
        createTransaction(acc2, acc1, "TX2");

        List<Transaction> result =
                transactionRepository.findAllByUserId(user.getId());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findAllByUserId should return empty when user has no transactions")
    void shouldReturnEmptyWhenUserHasNoTransactions() {

        User user = createUser("john");

        createAccount(user, "A1");

        List<Transaction> result =
                transactionRepository.findAllByUserId(user.getId());

        assertTrue(result.isEmpty());
    }
}
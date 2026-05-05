package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
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
    private EntityManager entityManager;

    // ---------- Helpers ----------

    private User createUser(String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .passwordHash("pass")
                .fullName(username)
                .build();

        entityManager.persist(user);
        return user;
    }

    private Account createAccount(User user, String accNo) {
        Account acc = Account.builder()
                .accountNumber(accNo)
                .user(user)
                .balance(BigDecimal.TEN)
                .isActive(true)
                .build();

        entityManager.persist(acc);
        return acc;
    }

    private Transaction createTx(Account sender, Account receiver, String ref) {
        Transaction tx = Transaction.builder()
                .referenceNumber(ref)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(BigDecimal.valueOf(100))
                .transactionType("TRANSFER")
                .build();

        entityManager.persist(tx);
        entityManager.flush();
        return tx;
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("findAllByAccountId should return sender/receiver transactions")
    void shouldFindTransactionsByAccountId() {
        User user = createUser("john");
        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTx(acc1, acc2, "TX1");
        createTx(acc2, acc1, "TX2");

        entityManager.flush();

        List<Transaction> result =
                transactionRepository.findAllByAccountId(acc1.getId());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findAllByAccountIdPaged should return paginated results")
    void shouldReturnPagedTransactions() {
        User user = createUser("john");
        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTx(acc1, acc2, "TX1");
        createTx(acc1, acc2, "TX2");
        createTx(acc1, acc2, "TX3");

        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Transaction> page =
                transactionRepository.findAllByAccountIdPaged(acc1.getId(), pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("findAllWithDetails should return all transactions")
    void shouldFindAllWithDetails() {
        User user = createUser("john");
        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTx(acc1, acc2, "TX1");
        createTx(acc1, acc2, "TX2");

        entityManager.flush();

        List<Transaction> result = transactionRepository.findAllWithDetails();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findAllByUserId should return user transactions")
    void shouldFindTransactionsByUserId() {
        User user = createUser("john");
        Account acc1 = createAccount(user, "A1");
        Account acc2 = createAccount(user, "A2");

        createTx(acc1, acc2, "TX1");
        createTx(acc2, acc1, "TX2");

        entityManager.flush();

        List<Transaction> result =
                transactionRepository.findAllByUserId(user.getId());

        assertEquals(2, result.size());
    }
}
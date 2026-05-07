package com.example.banking.service;

import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.model.Account;
import com.example.banking.model.Role;
import com.example.banking.model.Transaction;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;

    private User user;
    private User admin;
    private Account sender;
    private Account receiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .username("john")
                .roles(Set.of())
                .build();

        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        admin = User.builder()
                .id(2L)
                .username("admin")
                .roles(Set.of(adminRole))
                .build();

        sender = Account.builder()
                .id(10L)
                .accountNumber("ACC1")
                .balance(BigDecimal.valueOf(1000))
                .user(user)
                .build();

        receiver = Account.builder()
                .id(20L)
                .accountNumber("ACC2")
                .balance(BigDecimal.valueOf(500))
                .user(admin)
                .build();
    }

    // ---------------- TRANSFER SUCCESS ----------------

    @Test
    void shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("test transfer");

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response =
                transactionService.transfer(request, "john");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.getAmount());
        assertEquals("ACC1", response.getSenderAccountNumber());
        assertEquals("ACC2", response.getReceiverAccountNumber());

        assertEquals(BigDecimal.valueOf(800), sender.getBalance());
        assertEquals(BigDecimal.valueOf(700), receiver.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ---------------- SAME ACCOUNT ----------------

    @Test
    void shouldThrowException_whenSameAccountTransfer() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC1");
        request.setAmount(BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(request, "john"));
    }

    // ---------------- INSUFFICIENT FUNDS ----------------

    @Test
    void shouldThrowException_whenInsufficientFunds() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> transactionService.transfer(request, "john"));
    }

    // ---------------- ACCESS DENIED ----------------

    @Test
    void shouldThrowAccessDenied_whenNotOwnerOrAdmin() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        User otherUser = User.builder()
                .username("other")
                .roles(Set.of())
                .build();

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("other"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class,
                () -> transactionService.transfer(request, "other"));
    }

    // ---------------- GET BY ACCOUNT ----------------

    @Test
    void shouldGetTransactionsByAccount() {
        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("REF1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(receiver)
                .build();

        when(transactionRepository.findAllByAccountId(10L))
                .thenReturn(List.of(tx));

        var result =
                transactionService.getTransactionsByAccount("ACC1", "john");

        assertEquals(1, result.size());
        assertEquals("REF1", result.get(0).getReferenceNumber());
    }

    // ---------------- GET ALL ----------------

    @Test
    void shouldGetAllTransactions() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("REF1")
                .build();

        when(transactionRepository.findAllWithDetails())
                .thenReturn(List.of(tx));

        var result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
    }

    // ---------------- GET BY USERNAME ----------------

    @Test
    void shouldGetTransactionsByUsername() {
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(1L))
                .thenReturn(List.of(sender));

        Transaction tx = Transaction.builder()
                .id(1L)
                .senderAccount(sender)
                .build();

        when(transactionRepository.findAllByAccountId(10L))
                .thenReturn(List.of(tx));

        var result =
                transactionService.getTransactionsByUsername("john");

        assertEquals(1, result.size());
    }
}

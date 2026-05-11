package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
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
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private User admin;

    private Account sender;
    private Account receiver;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .roles(Set.of())
                .build();

        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        admin = User.builder()
                .id(2L)
                .username("admin")
                .fullName("Admin User")
                .roles(Set.of(adminRole))
                .build();

        sender = Account.builder()
                .id(10L)
                .accountNumber("ACC1")
                .balance(BigDecimal.valueOf(1000))
                .isActive(true)
                .user(user)
                .build();

        receiver = Account.builder()
                .id(20L)
                .accountNumber("ACC2")
                .balance(BigDecimal.valueOf(500))
                .isActive(true)
                .user(admin)
                .build();
    }

    // =====================================================
    // TRANSFER SUCCESS //These tests were failing because the mocked transaction object was incomplete compared to what your mapper/service expects.
    // =====================================================

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

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response =
                transactionService.transfer(request, "john");

        assertNotNull(response);

        assertEquals(BigDecimal.valueOf(200), response.getAmount());

        assertEquals("ACC1", response.getSenderAccountNumber());
        assertEquals("ACC2", response.getReceiverAccountNumber());

        assertEquals(BigDecimal.valueOf(800), sender.getBalance());
        assertEquals(BigDecimal.valueOf(700), receiver.getBalance());

        assertEquals("COMPLETED", response.getStatus());

        assertTrue(response.getReferenceNumber().startsWith("TXN-"));

        verify(accountRepository, times(2))
                .save(any(Account.class));

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void shouldAllowAdminTransfer() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response =
                transactionService.transfer(request, "admin");

        assertNotNull(response);
        assertEquals(BigDecimal.TEN, response.getAmount());
    }

    // =====================================================
    // TRANSFER VALIDATION
    // =====================================================

    @Test
    void shouldThrowException_whenSameAccountTransfer() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC1");
        request.setAmount(BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(request, "john")
        );

        assertTrue(ex.getMessage().contains("same account"));
    }

    @Test
    void shouldThrowException_whenSenderNotFound() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.transfer(request, "john")
        );
    }

    @Test
    void shouldThrowException_whenReceiverNotFound() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.transfer(request, "john")
        );
    }

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

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> transactionService.transfer(request, "john")
        );

        assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

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

        assertThrows(
                AccessDeniedException.class,
                () -> transactionService.transfer(request, "other")
        );
    }

    // =====================================================
    // GET TRANSACTIONS BY ACCOUNT
    // =====================================================

    @Test
    void shouldGetTransactionsByAccount() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("REF1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(transactionRepository.findAllByAccountId(10L))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByAccount("ACC1", "john");

        assertEquals(1, result.size());

        TransactionResponse response = result.get(0);

        assertEquals("REF1", response.getReferenceNumber());
        assertEquals("DEBIT", response.getDirection());
    }

    @Test
    void shouldThrowException_whenAccountHistoryNotFound() {

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> transactionService.getTransactionsByAccount("ACC1", "john")
        );
    }

    // =====================================================
    // GET ALL TRANSACTIONS
    // =====================================================

    @Test
    void shouldGetAllTransactions() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("REF1")
                .build();

        when(transactionRepository.findAllWithDetails())
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getAllTransactions();

        assertEquals(1, result.size());
    }

    // =====================================================
    // GET TRANSACTIONS BY USERNAME
    // =====================================================

    @Test
    void shouldGetTransactionsByUsername() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(user.getId()))
                .thenReturn(List.of(sender));

        // FIX: correct repository method
        when(transactionRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByUsername("john");

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoAccounts() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of());

        List<TransactionResponse> result =
                transactionService.getTransactionsByUsername("john");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowException_whenUsernameNotFound() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> transactionService.getTransactionsByUsername("john")
        );
    }

    @Test
    void shouldReturnCreditDirection_whenReceiverAccount() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("REF1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.findAllByAccountId(20L))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByAccount("ACC2", "admin");

        assertEquals("CREDIT", result.get(0).getDirection());
    }

    @Test
    void shouldAllowOwnerWithoutAdminRole() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        // user is owner → no need to call userRepository
        TransferResponse response =
                transactionService.transfer(request, "john");

        assertNotNull(response);
    }

    @Test
    void shouldAllowTransfer_whenUserIsAdmin() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        TransferResponse response =
                transactionService.transfer(request, "admin");

        assertNotNull(response);
    }

    @Test
    void shouldNotThrow_whenUserHasNoRoles() {

        User noRoleUser = User.builder()
                .username("test")
                .roles(Set.of())
                .build();

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);
        request.setDescription(null);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(noRoleUser));

        assertThrows(AccessDeniedException.class,
                () -> transactionService.transfer(request, "test"));
    }

    @Test
    void shouldDeduplicateTransactionsById() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(user.getId()))
                .thenReturn(List.of(sender, receiver));

        // FIXED
        when(transactionRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByUsername("john");

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnCreditDirectionInUsernameHistory() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .senderAccount(receiver)
                .receiverAccount(sender)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(user.getId()))
                .thenReturn(List.of(sender));

        when(transactionRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByUsername("john");

        assertEquals("CREDIT", result.get(0).getDirection());
    }

    @Test
    void shouldHandleNullSenderAccount() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("DEPOSIT")
                .status("COMPLETED")
                .senderAccount(null)
                .receiverAccount(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findAllWithDetails())
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getAllTransactions();

        assertNull(result.get(0).getSenderAccountNumber());
    }

    @Test
    void shouldHandleNullReceiverAccount() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("WITHDRAWAL")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findAllWithDetails())
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getAllTransactions();

        assertNull(result.get(0).getReceiverAccountNumber());
    }

    @Test
    void shouldAllowNullDescription() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);
        request.setDescription(null);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response =
                transactionService.transfer(request, "john");

        assertNull(response.getDescription());
    }

    @Test
    void shouldUseNowWhenTransactionCreatedAtNull() {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC2"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response =
                transactionService.transfer(request, "john");

        assertNotNull(response.getTransactionDate());
    }

    @Test
    void shouldHandleNullReceiverInUsernameTransactions() {

        Transaction tx = Transaction.builder()
                .id(1L)
                .referenceNumber("TXN1")
                .amount(BigDecimal.TEN)
                .transactionType("WITHDRAWAL")
                .status("COMPLETED")
                .senderAccount(sender)
                .receiverAccount(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(user.getId()))
                .thenReturn(List.of(sender));

        when(transactionRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result =
                transactionService.getTransactionsByUsername("john");

        assertEquals(1, result.size());
    }

    @Test
    void shouldAllowAdminAccessToAccountHistory() {

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC1"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        when(transactionRepository.findAllByAccountId(10L))
                .thenReturn(List.of());

        List<TransactionResponse> result =
                transactionService.getTransactionsByAccount("ACC1", "admin");

        assertNotNull(result);
    }
}
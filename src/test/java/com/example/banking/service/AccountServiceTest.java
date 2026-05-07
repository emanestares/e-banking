package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.model.Account;
import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .roles(new HashSet<>())
                .build();

        account = Account.builder()
                .id(10L)
                .accountNumber("ACC123")
                .balance(BigDecimal.valueOf(100))
                .accountType("SAVINGS")
                .isActive(true)
                .user(user)
                .build();
    }

    @Test
    void shouldReturnAccount_whenOwnerRequestsOwnAccount() {
        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC123"))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                accountService.getAccountByNumber("ACC123", "john");

        assertNotNull(response);
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("john", response.getOwnerUsername());
    }

    @Test
    void shouldAllowAdminToAccessAnyAccount() {
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();

        User admin = User.builder()
                .username("admin")
                .roles(Set.of(adminRole))
                .build();

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC123"))
                .thenReturn(Optional.of(account));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        AccountResponse response =
                accountService.getAccountByNumber("ACC123", "admin");

        assertNotNull(response);
        assertEquals("ACC123", response.getAccountNumber());
    }

    @Test
    void shouldThrowAccessDenied_whenNonOwnerAndNotAdmin() {
        User otherUser = User.builder()
                .username("other")
                .roles(Set.of()) // no admin role
                .build();

        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC123"))
                .thenReturn(Optional.of(account));

        when(userRepository.findByUsername("other"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                accountService.getAccountByNumber("ACC123", "other"));
    }

    @Test
    void shouldThrowException_whenAccountNotFound() {
        when(accountRepository.findByAccountNumberAndIsActiveTrue("ACC123"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                accountService.getAccountByNumber("ACC123", "john"));
    }

    @Test
    void shouldReturnAccountsByUsername() {
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of(account));

        List<AccountResponse> responses =
                accountService.getAccountsByUsername("john");

        assertEquals(1, responses.size());
        assertEquals("ACC123", responses.get(0).getAccountNumber());
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                accountService.getAccountsByUsername("john"));
    }

    @Test
    void shouldReturnAllAccounts() {
        when(accountRepository.findAllActiveAccountsWithUsers())
                .thenReturn(List.of(account));

        List<AccountResponse> responses = accountService.getAllAccounts();

        assertEquals(1, responses.size());
        assertEquals("ACC123", responses.get(0).getAccountNumber());
    }

    @Test
    void shouldMapAccountToResponseCorrectly() {
        when(accountRepository.findAllActiveAccountsWithUsers())
                .thenReturn(List.of(account));

        AccountResponse response =
                accountService.getAllAccounts().get(0);

        assertEquals(account.getId(), response.getId());
        assertEquals(account.getAccountNumber(), response.getAccountNumber());
        assertEquals(account.getBalance(), response.getBalance());
        assertEquals(account.getAccountType(), response.getAccountType());
        assertEquals(account.getIsActive(), response.getIsActive());
        assertEquals(user.getFullName(), response.getOwnerFullName());
        assertEquals(user.getUsername(), response.getOwnerUsername());
        assertEquals(user.getId(), response.getOwnerId());
    }
}

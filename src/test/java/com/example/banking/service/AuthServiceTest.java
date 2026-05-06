package com.example.banking.service;

import com.example.banking.config.JwtUtils;
import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.model.Account;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- SUCCESS WITH ACCOUNT ----------------

    @Test
    void shouldLoginSuccessfully_withAccount() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .build();

        Account account = Account.builder()
                .id(10L)
                .accountNumber("ACC123")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of(account));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("john", response.getUsername());
        assertEquals("John Doe", response.getFullName());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals(10L, response.getAccountId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(auth);
    }

    // ---------------- SUCCESS WITHOUT ACCOUNTS ----------------

    @Test
    void shouldLoginSuccessfully_withoutAccounts() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of());

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNull(response.getAccountId());
        assertNull(response.getAccountNumber());
    }

    // ---------------- USER NOT FOUND ----------------

    @Test
    void shouldThrowException_whenUserNotFound() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of()
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("User not found", ex.getMessage());
    }

    // ---------------- ROLE MAPPING ----------------

    @Test
    void shouldMapRolesCorrectly() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of());

        LoginResponse response = authService.login(request);

        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        assertEquals(2, response.getRoles().size());
    }
}
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
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLoginSuccessfully_withAccount() {
        // ---------- Arrange ----------
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

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtils.generateToken(authentication))
                .thenReturn("jwt-token");

        when(authentication.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of(account));

        // ---------- Act ----------
        LoginResponse response = authService.login(request);

        // ---------- Assert ----------
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("john", response.getUsername());
        assertEquals("John Doe", response.getFullName());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals(10L, response.getAccountId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(authentication);
    }

    @Test
    void shouldLoginSuccessfully_withoutAccounts() {
        // ---------- Arrange ----------
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtils.generateToken(authentication))
                .thenReturn("jwt-token");

        when(authentication.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of()); // no accounts

        // ---------- Act ----------
        LoginResponse response = authService.login(request);

        // ---------- Assert ----------
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNull(response.getAccountId());
        assertNull(response.getAccountNumber());
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        // ---------- Arrange ----------
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtils.generateToken(authentication))
                .thenReturn("jwt-token");

        when(authentication.getAuthorities())
                .thenReturn(List.of());

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        // ---------- Act + Assert ----------
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldMapRolesCorrectly() {
        // ---------- Arrange ----------
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtils.generateToken(authentication))
                .thenReturn("jwt-token");

        when(authentication.getAuthorities())
                .thenReturn(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findActiveAccountsByUserId(1L))
                .thenReturn(List.of());

        // ---------- Act ----------
        LoginResponse response = authService.login(request);

        // ---------- Assert ----------
        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        assertEquals(2, response.getRoles().size());
    }
}

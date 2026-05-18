package com.example.banking.service;

import com.example.banking.config.JwtUtils;
import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.LoginResponse;
import com.example.banking.dto.SignupRequest;
import com.example.banking.model.Account;
import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.RoleRepository;
import com.example.banking.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .email("john@example.com")
                .build();
    }

    // =====================================================
    // LOGIN TESTS
    // =====================================================

    @Test
    void shouldLoginSuccessfully_withAccount() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

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

        assertEquals(10L, response.getAccountId());
        assertEquals("ACC123", response.getAccountNumber());

        assertTrue(response.getRoles().contains("ROLE_USER"));

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtUtils).generateToken(auth);
    }

    @Test
    void shouldLoginSuccessfully_withoutAccounts() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

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

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldMapRolesCorrectly() {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

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

        assertEquals(2, response.getRoles().size());

        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }

    // =====================================================
    // REGISTER TESTS
    // =====================================================

    @Test
    void shouldRegisterSuccessfully() {

        SignupRequest request = new SignupRequest();
        request.setUsername("john");
        request.setPassword("password");
        request.setEmail("john@example.com");
        request.setFullName("John Doe");
        request.setInitialDeposit(BigDecimal.valueOf(500));

        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        when(accountRepository.existsByAccountNumber(any()))
                .thenReturn(false);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        LoginResponse response = authService.register(request);

        assertNotNull(response);

        assertEquals("john", response.getUsername());
        assertEquals("John Doe", response.getFullName());
        assertEquals("jwt-token", response.getToken());

        assertEquals(10L, response.getAccountId());

        assertNotNull(response.getAccountNumber());
        assertTrue(response.getAccountNumber().startsWith("ACC-"));

        verify(passwordEncoder).encode("password");

        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowException_whenUsernameAlreadyExists() {

        SignupRequest request = new SignupRequest();
        request.setUsername("john");

        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertTrue(ex.getMessage().contains("Username is already taken"));
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists() {

        SignupRequest request = new SignupRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertTrue(ex.getMessage().contains("Email is already in use"));
    }

    @Test
    void shouldThrowException_whenRoleNotFound() {

        SignupRequest request = new SignupRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertTrue(ex.getMessage().contains("ROLE_USER not found"));
    }

    @Test
    void shouldDefaultDepositToZero_whenInitialDepositIsNull() {

        SignupRequest request = new SignupRequest();
        request.setUsername("john");
        request.setPassword("password");
        request.setEmail("john@example.com");
        request.setFullName("John Doe");
        request.setInitialDeposit(null);

        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userRepository.existsByUsername(any()))
                .thenReturn(false);

        when(userRepository.existsByEmail(any()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        when(accountRepository.existsByAccountNumber(any()))
                .thenReturn(false);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtUtils.generateToken(auth))
                .thenReturn("jwt-token");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.register(request);

        assertNotNull(response);

        verify(accountRepository).save(argThat(account ->
                account.getBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }
}
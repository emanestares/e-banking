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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountRepository.findActiveAccountsByUserId(user.getId());
        Long accountId = accounts.isEmpty() ? null : accounts.get(0).getId();
        String accountNumber = accounts.isEmpty() ? null : accounts.get(0).getAccountNumber();

        return new LoginResponse(jwt, user.getUsername(), user.getFullName(),
                roles, accountId, accountNumber);
    }

    @Transactional
    public LoginResponse register(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + request.getEmail());
        }

        // Roles table must have ROLE_USER seeded
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException(
                        "ROLE_USER not found in database. Please seed the roles table."));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        BigDecimal deposit = (request.getInitialDeposit() != null &&
                              request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0)
                ? request.getInitialDeposit()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .balance(deposit)
                .accountType("SAVINGS")
                .isActive(true)
                .build();

        accountRepository.save(account);

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(jwt, user.getUsername(), user.getFullName(),
                roles, account.getId(), account.getAccountNumber());
    }

    private String generateAccountNumber() {
        String number;
        do {
            int rand = (int)(Math.random() * 90000) + 10000;
            number = "ACC-" + rand;
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}

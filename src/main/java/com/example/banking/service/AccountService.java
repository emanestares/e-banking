package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.EnrollRequest;
import com.example.banking.model.Account;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.repository.LimiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LimiterRepository limiterRepository;


    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber, String requestingUsername) {
        Account account = accountRepository.findByAccountNumberAndIsActiveTrue(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        // Customers can only view their own accounts
        if (!account.getUser().getUsername().equals(requestingUsername)) {
            boolean isAdmin = userRepository.findByUsername(requestingUsername)
                    .map(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .orElse(false);
            if (!isAdmin) {
                throw new AccessDeniedException("Access denied to account: " + accountNumber);
            }
        }
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return accountRepository.findActiveAccountsByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAllActiveAccountsWithUsers()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse enrollAccount(String username, EnrollRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String type = (request.getAccountType() != null &&
                request.getAccountType().equalsIgnoreCase("CHECKING"))
                ? "CHECKING" : "SAVINGS";

        BigDecimal deposit = (request.getInitialDeposit() != null &&
                request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0)
                ? request.getInitialDeposit()
                : BigDecimal.ZERO;

        // ── DYNAMIC LIMITER CHECK ──────────────────────────────────────────
        BigDecimal maxEnroll = limiterRepository.findByLimiterKey("MAX_ENROLL_AMOUNT")
                .map(l -> new BigDecimal(l.getLimiterValue()))
                .orElse(new BigDecimal("10000000")); // Fallback if record missing

        if (deposit.compareTo(maxEnroll) > 0) {
            throw new IllegalArgumentException("Initial deposit exceeds the maximum limit of " + String.format("%,.2f", maxEnroll));
        }
        // ───────────────────────────────────────────────────────────────────

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .balance(deposit)
                .accountType(type)
                .isActive(true)
                .build();

        accountRepository.save(account);
        return mapToResponse(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            int rand = (int)(Math.random() * 90000) + 10000;
            number = "ACC-" + rand;
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse resp = new AccountResponse();
        resp.setId(account.getId());
        resp.setAccountNumber(account.getAccountNumber());
        resp.setBalance(account.getBalance());
        resp.setAccountType(account.getAccountType());
        resp.setIsActive(account.getIsActive());
        resp.setCreatedAt(account.getCreatedAt());
        resp.setOwnerFullName(account.getUser().getFullName());
        resp.setOwnerUsername(account.getUser().getUsername());
        resp.setOwnerId(account.getUser().getId());
        return resp;
    }
}
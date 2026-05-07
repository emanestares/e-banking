package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    // -------------------------------------------------------
    // FUND TRANSFER
    // -------------------------------------------------------
    @Transactional
    public TransferResponse transfer(TransferRequest request, String requestingUsername) {

        // 1. Same-account check (Prevent unnecessary DB hits)
        if (request.getSenderAccountNumber().equals(request.getReceiverAccountNumber())) {
            throw new IllegalArgumentException("You cannot transfer money to the same account.");
        }

        // 2. Fetch both accounts once (Using active-only lookup)
        Account sender = accountRepository.findByAccountNumberAndIsActiveTrue(request.getSenderAccountNumber())
                .orElseThrow(() -> new NoSuchElementException("The source account does not exist or is inactive."));

        Account receiver = accountRepository.findByAccountNumberAndIsActiveTrue(request.getReceiverAccountNumber())
                .orElseThrow(() -> new NoSuchElementException("The destination account number does not exist or is inactive."));

        // 3. Authorization: Ensure requester owns sender account (unless Admin)
        if (!sender.getUser().getUsername().equals(requestingUsername)) {
            boolean isAdmin = userRepository.findByUsername(requestingUsername)
                    .map(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .orElse(false);
            if (!isAdmin) {
                throw new AccessDeniedException("You do not own this account.");
            }
        }

        // 4. Sufficient funds check
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds. Available: " + sender.getBalance());
        }

        // 5. Update balances
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        // Changes persist automatically due to @Transactional; explicit save ensures immediate flush
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 6. Create transaction record
        String refNumber = generateReferenceNumber();
        Transaction txn = Transaction.builder()
                .referenceNumber(refNumber)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.getAmount())
                .transactionType("TRANSFER")
                .status("COMPLETED")
                .description(request.getDescription())
                .build();

        transactionRepository.save(txn);

        // 7. Build Response
        TransferResponse response = new TransferResponse();
        response.setReferenceNumber(refNumber);
        response.setStatus("COMPLETED");
        response.setAmount(request.getAmount());
        response.setSenderAccountNumber(sender.getAccountNumber());
        response.setReceiverAccountNumber(receiver.getAccountNumber());
        response.setDescription(request.getDescription());
        response.setTransactionDate(txn.getCreatedAt() != null ? txn.getCreatedAt() : LocalDateTime.now());
        response.setNewSenderBalance(sender.getBalance());
        return response;
    }

    // -------------------------------------------------------
    // TRANSACTION HISTORY BY ACCOUNT
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(
            String accountNumber, String requestingUsername) {

        Account account = accountRepository.findByAccountNumberAndIsActiveTrue(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        // Authorization
        if (!account.getUser().getUsername().equals(requestingUsername)) {
            boolean isAdmin = userRepository.findByUsername(requestingUsername)
                    .map(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .orElse(false);
            if (!isAdmin) {
                throw new AccessDeniedException("Access denied to account: " + accountNumber);
            }
        }

        return transactionRepository.findAllByAccountId(account.getId())
                .stream()
                .map(t -> mapToResponse(t, account.getId()))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // ADMIN: ALL TRANSACTIONS
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllWithDetails()
                .stream()
                .map(t -> mapToResponse(t, null))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // TRANSACTION HISTORY BY USERNAME
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Account> accounts = accountRepository.findActiveAccountsByUserId(user.getId());
        if (accounts.isEmpty()) return List.of();

        // Collect all account IDs for this user
        java.util.Set<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(java.util.stream.Collectors.toSet());

        // Gather transactions from all accounts, deduplicate by id, sort by date descending
        return accounts.stream()
                .flatMap(acc -> transactionRepository.findAllByAccountId(acc.getId()).stream()
                        .map(t -> mapToResponse(t, acc.getId())))
                .collect(java.util.LinkedHashMap<Long, TransactionResponse>::new,
                        (map, t) -> map.putIfAbsent(t.getId(), t),
                        java.util.LinkedHashMap::putAll)
                .values().stream()
                .sorted(java.util.Comparator.comparing(
                        TransactionResponse::getCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .collect(java.util.stream.Collectors.toList());
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------
    private String generateReferenceNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + date + "-" + uuid;
    }

    private TransactionResponse mapToResponse(Transaction txn, Long perspectiveAccountId) {
        TransactionResponse resp = new TransactionResponse();
        resp.setId(txn.getId());
        resp.setReferenceNumber(txn.getReferenceNumber());
        resp.setAmount(txn.getAmount());
        resp.setTransactionType(txn.getTransactionType());
        resp.setStatus(txn.getStatus());
        resp.setDescription(txn.getDescription());
        resp.setCreatedAt(txn.getCreatedAt());
        resp.setSenderAccountNumber(
                txn.getSenderAccount() != null ? txn.getSenderAccount().getAccountNumber() : null);
        resp.setReceiverAccountNumber(
                txn.getReceiverAccount() != null ? txn.getReceiverAccount().getAccountNumber() : null);
        resp.setSenderName(
                txn.getSenderAccount() != null ? txn.getSenderAccount().getUser().getFullName() : null);
        resp.setReceiverName(
                txn.getReceiverAccount() != null ? txn.getReceiverAccount().getUser().getFullName() : null);

        // Determine DEBIT/CREDIT direction from perspective account
        if (perspectiveAccountId != null) {
            if (txn.getSenderAccount() != null
                    && txn.getSenderAccount().getId().equals(perspectiveAccountId)) {
                resp.setDirection("DEBIT");
            } else {
                resp.setDirection("CREDIT");
            }
        }
        return resp;
    }
}
package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.ApiResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.AccountService;
import com.example.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AccountService accountService;
    @Autowired private TransactionService transactionService;

    // View all accounts
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAllAccounts()));
    }

    // View all transactions
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getAllTransactions()));
    }
}
package com.example.banking.controller;

import com.example.banking.dto.ApiResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Transfer funds
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransferResponse response =
                transactionService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Transfer successful", response));
    }

    // Get transaction history for a specific account
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getByAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions =
                transactionService.getTransactionsByAccount(accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }

    // Get transaction history for the logged-in user
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions =
                transactionService.getTransactionsByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
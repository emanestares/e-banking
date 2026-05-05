package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.ApiResponse;
import com.example.banking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Get all accounts belonging to the logged-in customer
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AccountResponse> accounts =
                accountService.getAccountsByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(accounts));
    }

    // Get a specific account by number (customers can only access their own)
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse account =
                accountService.getAccountByNumber(accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(account));
    }
}
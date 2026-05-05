package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    private String username;
    private String fullName;
    private List<String> roles;
    private Long accountId;
    private String accountNumber;

    public LoginResponse(String token, String username, String fullName,
                         List<String> roles, Long accountId, String accountNumber) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
    }
}
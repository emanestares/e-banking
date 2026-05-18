package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetails mockUser() {
        return User.withUsername("john")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void shouldReturnMyAccounts() throws Exception {

        AccountResponse account = new AccountResponse();
        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountService.getAccountsByUsername("john"))
                .thenReturn(List.of(account));

        mockMvc.perform(get("/accounts/my")
                        .with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].accountNumber").value("ACC123"))
                .andExpect(jsonPath("$.data[0].balance").value(1000.00));
    }

    @Test
    void shouldReturnAccountByNumber() throws Exception {

        AccountResponse account = new AccountResponse();
        account.setId(2L);
        account.setAccountNumber("ACC999");
        account.setBalance(new BigDecimal("500.00"));

        when(accountService.getAccountByNumber("ACC999", "john"))
                .thenReturn(account);

        mockMvc.perform(get("/accounts/ACC999")
                        .with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC999"))
                .andExpect(jsonPath("$.data.balance").value(500.00));
    }
}
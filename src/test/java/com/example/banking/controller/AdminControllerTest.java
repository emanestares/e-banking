package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.AccountService;
import com.example.banking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    // ---------------- ADMIN SUCCESS CASE ----------------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllAccountsForAdmin() throws Exception {

        AccountResponse account = new AccountResponse();
        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountService.getAllAccounts())
                .thenReturn(List.of(account));

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].accountNumber").value("ACC123"))
                .andExpect(jsonPath("$.data[0].balance").value(1000.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllTransactionsForAdmin() throws Exception {

        TransactionResponse txn = new TransactionResponse();
        txn.setId(1L);
        txn.setReferenceNumber("TXN-001");
        txn.setAmount(new BigDecimal("500.00"));

        when(transactionService.getAllTransactions())
                .thenReturn(List.of(txn));

        mockMvc.perform(get("/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].referenceNumber").value("TXN-001"))
                .andExpect(jsonPath("$.data[0].amount").value(500.00));
    }

    // ---------------- ACCESS DENIED CASE ----------------

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void shouldForbidAccess_whenNotAdmin() throws Exception {

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void shouldForbidTransactionAccess_whenNotAdmin() throws Exception {

        mockMvc.perform(get("/admin/transactions"))
                .andExpect(status().isForbidden());
    }

    // ---------------- UNAUTHENTICATED ----------------

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
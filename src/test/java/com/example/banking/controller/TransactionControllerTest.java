package com.example.banking.controller;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetails mockUser() {
        return User.withUsername("john")
                .password("password")
                .roles("USER")
                .build();
    }

    // ---------------- TRANSFER ----------------

    @Test
    void shouldTransferMoneySuccessfully() throws Exception {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Rent payment");

        TransferResponse response = new TransferResponse();
        response.setReferenceNumber("TXN-001");
        response.setStatus("COMPLETED");
        response.setAmount(new BigDecimal("100.00"));
        response.setSenderAccountNumber("ACC1");
        response.setReceiverAccountNumber("ACC2");
        response.setNewSenderBalance(new BigDecimal("900.00"));

        when(transactionService.transfer(request, "john"))
                .thenReturn(response);

        mockMvc.perform(post("/transactions/transfer")
                        .with(user(mockUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer successful"))
                .andExpect(jsonPath("$.data.referenceNumber").value("TXN-001"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    // ---------------- ACCOUNT TRANSACTIONS ----------------

    @Test
    void shouldGetTransactionsByAccount() throws Exception {

        TransactionResponse txn = new TransactionResponse();
        txn.setId(1L);
        txn.setReferenceNumber("TXN-123");
        txn.setAmount(new BigDecimal("200.00"));

        when(transactionService.getTransactionsByAccount("ACC1", "john"))
                .thenReturn(List.of(txn));

        mockMvc.perform(get("/transactions/account/ACC1")
                        .with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].referenceNumber").value("TXN-123"))
                .andExpect(jsonPath("$.data[0].amount").value(200.00));
    }

    // ---------------- USER TRANSACTIONS ----------------

    @Test
    void shouldGetMyTransactions() throws Exception {

        TransactionResponse txn = new TransactionResponse();
        txn.setId(2L);
        txn.setReferenceNumber("TXN-999");
        txn.setAmount(new BigDecimal("500.00"));

        when(transactionService.getTransactionsByUsername("john"))
                .thenReturn(List.of(txn));

        mockMvc.perform(get("/transactions/my")
                        .with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].referenceNumber").value("TXN-999"))
                .andExpect(jsonPath("$.data[0].amount").value(500.00));
    }

    // ---------------- VALIDATION FAILURE ----------------

    @Test
    void shouldFailTransfer_whenInvalidPayload() throws Exception {

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber(""); // invalid
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("0.00"));

        mockMvc.perform(post("/transactions/transfer")
                        .with(user(mockUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
package com.example.banking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransferRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------------- VALID CASE ----------------

    @Test
    void shouldPassValidation_whenRequestIsValid() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Payment");

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ---------------- SENDER ACCOUNT ----------------

    @Test
    void shouldFail_whenSenderAccountIsBlank() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("10.00"));

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("senderAccountNumber")));
    }

    // ---------------- RECEIVER ACCOUNT ----------------

    @Test
    void shouldFail_whenReceiverAccountIsBlank() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("");

        request.setAmount(new BigDecimal("10.00"));

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("receiverAccountNumber")));
    }

    // ---------------- AMOUNT NULL ----------------

    @Test
    void shouldFail_whenAmountIsNull() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");

        request.setAmount(null);

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    // ---------------- AMOUNT MIN VALUE ----------------

    @Test
    void shouldFail_whenAmountLessThanMinimum() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("0.00"));

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals("Amount must be greater than 0"))
        );
    }

    // ---------------- AMOUNT DIGITS ----------------

    @Test
    void shouldFail_whenAmountHasTooManyDecimals() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("10.123")); // invalid scale

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Invalid amount format")));
    }

    // ---------------- DESCRIPTION LENGTH ----------------

    @Test
    void shouldFail_whenDescriptionTooLong() {
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1");
        request.setReceiverAccountNumber("ACC2");
        request.setAmount(new BigDecimal("10.00"));

        request.setDescription("a".repeat(600));

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }
}
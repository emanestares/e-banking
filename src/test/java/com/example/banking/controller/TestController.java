package com.example.banking.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
class TestController {

    @GetMapping("/validation")
    public void validation() {
        throw new RuntimeException("Validation failed");
    }

    @GetMapping("/bad-credentials")
    public void badCredentials() {
        throw new BadCredentialsException("bad login");
    }

    @GetMapping("/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("no access");
    }

    @GetMapping("/illegal-state")
    public void illegalState() {
        throw new IllegalStateException("insufficient funds");
    }

    @GetMapping("/illegal-arg")
    public void illegalArg() {
        throw new IllegalArgumentException("invalid request");
    }

    @GetMapping("/runtime")
    public void runtime() {
        throw new RuntimeException("not found");
    }

    @GetMapping("/generic")
    public void generic() throws Exception {
        throw new Exception("system crash");
    }
}

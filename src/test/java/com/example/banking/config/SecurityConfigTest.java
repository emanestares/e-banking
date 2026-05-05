package com.example.banking.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void shouldLoadSecurityFilterChain() {
        assertNotNull(securityFilterChain);
    }

    @Test
    void shouldCreatePasswordEncoder() {
        assertNotNull(passwordEncoder);

        String raw = "password";
        String encoded = passwordEncoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(passwordEncoder.matches(raw, encoded));
    }

    @Test
    void shouldCreateAuthenticationProvider() {
        assertNotNull(authenticationProvider);
        assertNotNull(authenticationProvider.getPasswordEncoder());
    }

    @Test
    void shouldInjectAuthenticationManager() {
        assertNotNull(authenticationManager);
    }

    @Test
    void shouldCreateCorsConfiguration() {
        assertNotNull(corsConfigurationSource);
    }
}

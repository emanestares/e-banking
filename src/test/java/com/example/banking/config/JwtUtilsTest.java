package com.example.banking.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private final String SECRET = "mySuperSecretKeyThatIsLongEnoughForHS256Algorithm12345";
    private final int EXPIRATION = 1000 * 60; // 1 minute

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();

        // Inject values into private fields
        setField(jwtUtils, "jwtSecret", SECRET);
        setField(jwtUtils, "jwtExpirationMs", EXPIRATION);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtUtils.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void shouldGenerateTokenFromAuthentication() {
        UserDetails userDetails =
                new User("john", "password", Collections.emptyList());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateToken(authentication);

        assertNotNull(token);
        assertEquals("john", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void shouldGenerateTokenFromUsername() {
        String token = jwtUtils.generateTokenFromUsername("alice");

        assertNotNull(token);
        assertEquals("alice", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtUtils.generateTokenFromUsername("validUser");

        boolean isValid = jwtUtils.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";

        boolean isValid = jwtUtils.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForExpiredToken() throws InterruptedException {

        try {
            setField(jwtUtils, "jwtExpirationMs", 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String token = jwtUtils.generateTokenFromUsername("bob");

        Thread.sleep(5);

        boolean isValid = jwtUtils.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtils.generateTokenFromUsername("charlie");

        String username = jwtUtils.getUsernameFromToken(token);

        assertEquals("charlie", username);
    }
}

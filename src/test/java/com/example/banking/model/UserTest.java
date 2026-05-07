package com.example.banking.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldBuildUserWithDefaults() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .passwordHash("hashed_password")
                .fullName("John Doe")
                .build();

        assertNotNull(user);
        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashed_password", user.getPasswordHash());
        assertEquals("John Doe", user.getFullName());

        // Defaults
        assertTrue(user.getIsActive());
        assertNotNull(user.getRoles());
        assertNotNull(user.getAccounts());
        assertTrue(user.getRoles().isEmpty());
        assertTrue(user.getAccounts().isEmpty());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        User user = new User();

        user.setId(1L);
        user.setUsername("jane");
        user.setEmail("jane@example.com");
        user.setPasswordHash("pass");
        user.setFullName("Jane Doe");
        user.setIsActive(false);

        assertEquals(1L, user.getId());
        assertEquals("jane", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("pass", user.getPasswordHash());
        assertEquals("Jane Doe", user.getFullName());
        assertFalse(user.getIsActive());
    }

    @Test
    void shouldInitializeTimestampsOnPrePersist() {
        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() throws InterruptedException {
        User user = new User();

        user.onCreate();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime initialUpdatedAt = user.getUpdatedAt();

        Thread.sleep(5);

        user.onUpdate();

        assertEquals(createdAt, user.getCreatedAt());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldAllowAddingRolesAndAccounts() {
        User user = User.builder()
                .username("test")
                .email("test@test.com")
                .passwordHash("pass")
                .fullName("Test User")
                .build();

        Role role = Role.builder().name("ROLE_USER").build();
        Account account = Account.builder().accountNumber("123").build();

        user.getRoles().add(role);
        user.getAccounts().add(account);

        assertEquals(1, user.getRoles().size());
        assertEquals(1, user.getAccounts().size());
    }

    @Test
    void shouldCreateUserUsingAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User(
                10L,
                "admin",
                "admin@test.com",
                "hash",
                "Admin User",
                true,
                now,
                now,
                null,
                null
        );

        assertEquals(10L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("admin@test.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Admin User", user.getFullName());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
}
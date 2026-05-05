package com.example.banking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldCreateRoleUsingBuilder() {
        Role role = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        assertNotNull(role);
        assertEquals(1L, role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    void shouldSetAndGetValuesCorrectly() {
        Role role = new Role();

        role.setId(2L);
        role.setName("ROLE_CUSTOMER");

        assertEquals(2L, role.getId());
        assertEquals("ROLE_CUSTOMER", role.getName());
    }

    @Test
    void shouldCreateRoleUsingAllArgsConstructor() {
        Role role = new Role(3L, "ROLE_MANAGER");

        assertEquals(3L, role.getId());
        assertEquals("ROLE_MANAGER", role.getName());
    }

    @Test
    void shouldCreateEmptyRoleUsingNoArgsConstructor() {
        Role role = new Role();

        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    void shouldAllowUpdatingName() {
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        role.setName("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", role.getName());
    }
}
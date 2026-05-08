package com.example.banking.repository;

import com.example.banking.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    // =====================================================
    // FIND BY NAME
    // =====================================================

    @Test
    @DisplayName("should find role by name")
    void shouldFindByName() {

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        entityManager.persist(role);
        entityManager.flush();

        Optional<Role> result =
                roleRepository.findByName("ROLE_ADMIN");

        assertTrue(result.isPresent());
        assertEquals("ROLE_ADMIN", result.get().getName());
    }

    @Test
    @DisplayName("should return empty when role not found")
    void shouldReturnEmptyWhenRoleNotFound() {

        Optional<Role> result =
                roleRepository.findByName("ROLE_UNKNOWN");

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // UNIQUE CONSTRAINT
    // =====================================================

    @Test
    @DisplayName("should enforce unique role name constraint")
    void shouldEnforceUniqueConstraint() {

        Role role1 = Role.builder()
                .name("ROLE_USER")
                .build();

        Role role2 = Role.builder()
                .name("ROLE_USER")
                .build();

        entityManager.persist(role1);
        entityManager.flush();

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(role2);
            entityManager.flush(); // exception happens HERE
        });
    }
}
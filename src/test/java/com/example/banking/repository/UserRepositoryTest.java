package com.example.banking.repository;

import com.example.banking.model.Role;
import com.example.banking.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    // =====================================================
    // HELPERS
    // =====================================================

    private Role createRole(String name) {
        Role role = Role.builder()
                .name(name)
                .build();
        return entityManager.persistAndFlush(role);
    }

    private User createUser(String username, String email, boolean active, Set<Role> roles) {

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("pass")
                .fullName(username)
                .isActive(active)
                .roles(roles)
                .build();

        return entityManager.persistAndFlush(user);
    }

    // =====================================================
    // FIND BY USERNAME
    // =====================================================

    @Test
    @DisplayName("findByUsername should return user")
    void shouldFindByUsername() {

        createUser("john", "john@test.com", true, Set.of());

        Optional<User> result =
                userRepository.findByUsername("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    @DisplayName("findByUsername should return empty when not found")
    void shouldReturnEmptyWhenUsernameNotFound() {

        Optional<User> result =
                userRepository.findByUsername("missing");

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // FIND BY EMAIL
    // =====================================================

    @Test
    @DisplayName("findByEmail should return user")
    void shouldFindByEmail() {

        createUser("john", "john@test.com", true, Set.of());

        Optional<User> result =
                userRepository.findByEmail("john@test.com");

        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());
    }

    // =====================================================
    // EXISTS BY USERNAME
    // =====================================================

    @Test
    @DisplayName("existsByUsername should return true when exists")
    void shouldCheckUsernameExists() {

        createUser("john", "john@test.com", true, Set.of());

        assertTrue(userRepository.existsByUsername("john"));
    }

    @Test
    @DisplayName("existsByUsername should return false when not exists")
    void shouldReturnFalseWhenUsernameNotExists() {

        assertFalse(userRepository.existsByUsername("ghost"));
    }

    // =====================================================
    // EXISTS BY EMAIL
    // =====================================================

    @Test
    @DisplayName("existsByEmail should return true when exists")
    void shouldCheckEmailExists() {

        createUser("john", "john@test.com", true, Set.of());

        assertTrue(userRepository.existsByEmail("john@test.com"));
    }

    @Test
    @DisplayName("existsByEmail should return false when not exists")
    void shouldReturnFalseWhenEmailNotExists() {

        assertFalse(userRepository.existsByEmail("none@test.com"));
    }

    // =====================================================
    // FIND ACTIVE CUSTOMERS
    // =====================================================

    @Test
    @DisplayName("findAllActiveCustomers should return only active ROLE_USER users")
    void shouldFindAllActiveCustomers() {

        Role userRole = createRole("ROLE_USER");
        Role adminRole = createRole("ROLE_ADMIN");

        createUser(
                "john",
                "john@test.com",
                true,
                Set.of(userRole)
        );

        createUser(
                "jane",
                "jane@test.com",
                true,
                Set.of(userRole)
        );

        // inactive user (should be excluded)
        createUser(
                "inactive",
                "inactive@test.com",
                false,
                Set.of(userRole)
        );

        // admin user (should be excluded)
        createUser(
                "admin",
                "admin@test.com",
                true,
                Set.of(adminRole)
        );

        List<User> result =
                userRepository.findAllActiveCustomers();

        assertEquals(2, result.size());

        assertTrue(
                result.stream().allMatch(User::getIsActive)
        );

        assertTrue(
                result.stream()
                        .allMatch(u ->
                                u.getRoles().stream()
                                        .anyMatch(r -> r.getName().equals("ROLE_USER"))
                        )
        );
    }

    @Test
    @DisplayName("findAllActiveCustomers should return empty when none match")
    void shouldReturnEmptyWhenNoActiveCustomers() {

        Role adminRole = createRole("ROLE_ADMIN");

        createUser(
                "admin",
                "admin@test.com",
                true,
                Set.of(adminRole)
        );

        List<User> result =
                userRepository.findAllActiveCustomers();

        assertTrue(result.isEmpty());
    }
}
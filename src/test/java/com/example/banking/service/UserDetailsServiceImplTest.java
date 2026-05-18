package com.example.banking.service;

import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    private User activeUser;

    @BeforeEach
    void setUp() {

        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        activeUser = User.builder()
                .username("john")
                .passwordHash("hashed-password")
                .isActive(true)
                .roles(Set.of(role))
                .build();
    }

    // =====================================================
    // SUCCESS CASE
    // =====================================================

    @Test
    void shouldLoadUserByUsernameSuccessfully() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(activeUser));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("john");

        assertNotNull(userDetails);

        assertEquals("john", userDetails.getUsername());
        assertEquals("hashed-password", userDetails.getPassword());

        assertEquals(1, userDetails.getAuthorities().size());

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_USER"::equals)
        );

        verify(userRepository).findByUsername("john");
    }

    // =====================================================
    // USER NOT FOUND
    // =====================================================

    @Test
    void shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("john")
        );

        assertEquals(
                "User not found with username: john",
                ex.getMessage()
        );
    }

    // =====================================================
    // USER DISABLED
    // =====================================================

    @Test
    void shouldThrowException_whenUserIsDisabled() {

        User disabledUser = User.builder()
                .username("john")
                .passwordHash("pass")
                .isActive(false)
                .roles(Set.of())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(disabledUser));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("john")
        );

        assertEquals(
                "Account is disabled: john",
                ex.getMessage()
        );
    }

    // =====================================================
    // MULTIPLE ROLES
    // =====================================================

    @Test
    void shouldMapMultipleRolesToAuthorities() {

        Role role1 = Role.builder()
                .name("ROLE_USER")
                .build();

        Role role2 = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        User user = User.builder()
                .username("admin")
                .passwordHash("pass")
                .isActive(true)
                .roles(Set.of(role1, role2))
                .build();

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("admin");

        assertEquals(2, userDetails.getAuthorities().size());

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
    }

    // =====================================================
    // ROLE NORMALIZATION
    // =====================================================

    @Test
    void shouldNormalizeRoleWithoutPrefix() {

        Role role = Role.builder()
                .name("ADMIN")
                .build();

        User user = User.builder()
                .username("admin")
                .passwordHash("pass")
                .isActive(true)
                .roles(Set.of(role))
                .build();

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("admin");

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
    }

    @Test
    void shouldNormalizeMixedRolesCorrectly() {

        Role role1 = Role.builder()
                .name("USER")
                .build();

        Role role2 = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        User user = User.builder()
                .username("mixed")
                .passwordHash("pass")
                .isActive(true)
                .roles(Set.of(role1, role2))
                .build();

        when(userRepository.findByUsername("mixed"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("mixed");

        assertEquals(2, userDetails.getAuthorities().size());

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );

        assertTrue(
                userDetails.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
    }

    // =====================================================
    // EMPTY ROLES
    // =====================================================

    @Test
    void shouldHandleUserWithNoRoles() {

        User user = User.builder()
                .username("noroles")
                .passwordHash("pass")
                .isActive(true)
                .roles(Set.of())
                .build();

        when(userRepository.findByUsername("noroles"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("noroles");

        assertNotNull(userDetails);

        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}
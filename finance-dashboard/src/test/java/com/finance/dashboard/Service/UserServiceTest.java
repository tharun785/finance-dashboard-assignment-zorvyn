package com.finance.dashboard.Service;


import com.finance.dashboard.dto.SignupRequest;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.RoleRepository;
import com.finance.dashboard.repository.UserRepository;

import com.finance.dashboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;
    private Role viewerRole;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("john");
        signupRequest.setEmail("john@test.com");
        signupRequest.setPassword("1234");

        viewerRole = new Role();
        viewerRole.setName(ERole.ROLE_VIEWER);
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded123");
        when(roleRepository.findByName(ERole.ROLE_VIEWER)).thenReturn(Optional.of(viewerRole));

        User savedUser = new User("john", "john@test.com", "encoded123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(signupRequest);

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.createUser(signupRequest));

        assertEquals("Username is already taken!", ex.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        User user = new User("john", "john@test.com", "pass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("john", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.getUserById(1L));
    }

    @Test
    void testUpdateUserStatus() {
        User user = new User("john", "john@test.com", "pass");
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUserStatus(1L, false);

        assertFalse(result.getActive());
    }

    @Test
    void testDeleteUser() {
        User user = new User("john", "john@test.com", "pass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(user);
    }
}

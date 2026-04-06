package com.finance.dashboard.service;

import com.finance.dashboard.dto.SignupRequest;
import com.finance.dashboard.model.ERole;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.RoleRepository;
import com.finance.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(SignupRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()));

        // Set roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            // Default role is VIEWER
            Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(viewerRole);
        } else {
            request.getRoles().forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(adminRole);
                        break;
                    case "analyst":
                        Role analystRole = roleRepository.findByName(ERole.ROLE_ANALYST)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(analystRole);
                        break;
                    default:
                        Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(viewerRole);
                }
            });
        }

        user.setRoles(roles);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User updateUserStatus(Long userId, Boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setActive(active);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return getUserByUsername(username);
    }

    public User updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            ERole eRole;
            switch (roleName.toUpperCase()) {
                case "ROLE_ADMIN":
                case "ADMIN":
                    eRole = ERole.ROLE_ADMIN;
                    break;
                case "ROLE_ANALYST":
                case "ANALYST":
                    eRole = ERole.ROLE_ANALYST;
                    break;
                default:
                    eRole = ERole.ROLE_VIEWER;
            }
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(role);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }
}
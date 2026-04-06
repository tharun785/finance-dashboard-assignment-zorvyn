package com.finance.dashboard.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.config.JwtAuthFilter;
import com.finance.dashboard.config.JwtService;
import com.finance.dashboard.controller.AuthController;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.dto.SignupRequest;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private User testUser;
    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("viewer"));

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@finance.com");
        testUser.setActive(true);

        authentication = mock(Authentication.class);
        userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn("admin");
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    // ========== LOGIN TESTS ==========

    @Test
    void testLogin_Success() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        when(userService.getUserByUsername("admin")).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@finance.com"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_MissingUsername() throws Exception {
        String invalidLogin = "{\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_MissingPassword() throws Exception {
        String invalidLogin = "{\"username\":\"admin\"}";

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_EmptyUsername() throws Exception {
        loginRequest.setUsername("");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_EmptyPassword() throws Exception {
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== SIGNUP TESTS ==========

    @Test
    void testSignup_Success() throws Exception {
        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void testSignup_DuplicateUsername() throws Exception {
        when(userService.createUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is already taken!"));
    }

    @Test
    void testSignup_DuplicateEmail() throws Exception {
        when(userService.createUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Email is already in use!"));

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is already in use!"));
    }

    @Test
    void testSignup_MissingUsername() throws Exception {
        signupRequest.setUsername(null);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_MissingEmail() throws Exception {
        signupRequest.setEmail(null);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_MissingPassword() throws Exception {
        signupRequest.setPassword(null);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_ShortPassword() throws Exception {
        signupRequest.setPassword("123");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_InvalidEmail() throws Exception {
        signupRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_ShortUsername() throws Exception {
        signupRequest.setUsername("ab");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== CREATE ADMIN TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdminUser_Success() throws Exception {
        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        String adminRequest = "{\"username\":\"newadmin\",\"email\":\"admin@test.com\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/create-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin user created successfully"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdminUser_DuplicateUsername() throws Exception {
        when(userService.createUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        String adminRequest = "{\"username\":\"existing\",\"email\":\"admin@test.com\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/create-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is already taken!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdminUser_MissingUsername() throws Exception {
        String adminRequest = "{\"email\":\"admin@test.com\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/create-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdminUser_MissingEmail() throws Exception {
        String adminRequest = "{\"username\":\"newadmin\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/create-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAdminUser_MissingPassword() throws Exception {
        String adminRequest = "{\"username\":\"newadmin\",\"email\":\"admin@test.com\"}";

        mockMvc.perform(post("/api/auth/create-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest))
                .andExpect(status().isBadRequest());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void testLogin_WithSpecialCharacters() throws Exception {
        loginRequest.setUsername("admin@#$");
        loginRequest.setPassword("pass@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSignup_WithWhitespaceInUsername() throws Exception {
        signupRequest.setUsername("  username  ");

        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignup_WithMultipleRoles() throws Exception {
        signupRequest.setRoles(Set.of("admin", "analyst"));

        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignup_WithNoRoles() throws Exception {
        signupRequest.setRoles(null);

        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    // ========== BOUNDARY TESTS ==========

    @Test
    void testLogin_MaxLengthUsername() throws Exception {
        String longUsername = "a".repeat(50);
        loginRequest.setUsername(longUsername);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSignup_MaxLengthPassword() throws Exception {
        signupRequest.setPassword("a".repeat(100));

        when(userService.createUser(any(SignupRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    // ========== PERFORMANCE TESTS ==========

    @Test
    void testLogin_ResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        when(userService.getUserByUsername("admin")).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assert responseTime < 2000 : "Response time should be less than 2 seconds";
    }
}
package com.finance.dashboard.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.config.JwtAuthFilter;
import com.finance.dashboard.config.JwtService;
import com.finance.dashboard.controller.UserController;
import com.finance.dashboard.dto.SignupRequest;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // ✅ FIXED

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetAllUsers() throws Exception {
        User user = new User("john", "john@test.com", "pass");

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk()); // ✅ now works
    }

    @Test
    void testCreateUser() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("john");
        request.setEmail("john@test.com");
        request.setPassword("1234");

        User user = new User("john", "john@test.com", "encoded");

        Mockito.when(userService.createUser(Mockito.any()))
                .thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // ✅ FIXED (filters disabled)
    }

    @Test
    void testGetUserById_Success() throws Exception {
        User user = new User("john", "john@test.com", "pass");

        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCurrentUser() throws Exception {
        User user = new User("john", "john@test.com", "pass");

        Mockito.when(userService.getCurrentAuthenticatedUser())
                .thenReturn(user);

        mockMvc.perform(get("/api/users/current"))
                .andExpect(status().isOk());
    }
}
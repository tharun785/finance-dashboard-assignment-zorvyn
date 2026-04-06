package com.finance.dashboard.Controller;


import com.finance.dashboard.controller.DashboardController;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.RecordService;
import com.finance.dashboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private RecordService recordService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardController controller;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("testUser");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null)
        );

        when(userService.getUserByUsername("testUser")).thenReturn(user);
    }

    @Test
    void testGetDashboardSummary() {
        RecordResponse income = new RecordResponse();
        income.setType("INCOME");
        income.setAmount(2000.0);

        RecordResponse expense = new RecordResponse();
        expense.setType("EXPENSE");
        expense.setAmount(500.0);
        expense.setCategory("Food");

        when(recordService.getUserRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(income, expense));

        ResponseEntity<?> response = controller.getDashboardSummary();

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(2000.0, body.get("totalIncome"));
        assertEquals(500.0, body.get("totalExpense"));
        assertEquals(1500.0, body.get("netBalance"));
    }

    @Test
    void testGetRecentActivity() {
        RecordResponse record1 = new RecordResponse();
        record1.setDate(LocalDate.now());
        record1.setAmount(100.0);

        RecordResponse record2 = new RecordResponse();
        record2.setDate(LocalDate.now().minusDays(1));
        record2.setAmount(200.0);

        when(recordService.getUserRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(record1, record2));

        ResponseEntity<?> response = controller.getRecentActivity();

        List<RecordResponse> result = (List<RecordResponse>) response.getBody();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getDate().isAfter(result.get(1).getDate()));
    }
}

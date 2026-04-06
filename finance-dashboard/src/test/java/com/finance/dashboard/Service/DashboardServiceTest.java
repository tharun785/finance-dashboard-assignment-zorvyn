package com.finance.dashboard.Service;


import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository repository;

    @InjectMocks
    private DashboardService service;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("testUser");
    }

    @Test
    void testGetDashboardSummary() {
        when(repository.sumByType("INCOME")).thenReturn(5000.0);
        when(repository.sumByType("EXPENSE")).thenReturn(2000.0);

        List<Object[]> mockCategory = new ArrayList<>();
        mockCategory.add(new Object[]{"Food", 1000.0});
        mockCategory.add(new Object[]{"Travel", 500.0});

        when(repository.getCategoryWiseTotals()).thenReturn(mockCategory);

        Map<String, Object> result = service.getDashboardSummary(user);

        assertEquals(5000.0, result.get("totalIncome"));
        assertEquals(2000.0, result.get("totalExpense"));
        assertEquals(3000.0, result.get("netBalance"));

        Map<String, Double> categoryWise = (Map<String, Double>) result.get("categoryWise");
        assertEquals(2, categoryWise.size());
        assertEquals(1000.0, categoryWise.get("Food"));
    }

    @Test
    void testGetTotalIncome() {
        when(repository.sumByType("INCOME")).thenReturn(1000.0);
        assertEquals(1000.0, service.getTotalIncome(user));
    }

    @Test
    void testGetTotalExpense() {
        when(repository.sumByType("EXPENSE")).thenReturn(800.0);
        assertEquals(800.0, service.getTotalExpense(user));
    }

    @Test
    void testGetNetBalance() {
        when(repository.sumByType("INCOME")).thenReturn(2000.0);
        when(repository.sumByType("EXPENSE")).thenReturn(500.0);

        assertEquals(1500.0, service.getNetBalance(user));
    }

    @Test
    void testGetCategoryWiseTotals() {
        List<Object[]> mockData = List.of(
                new Object[]{"Food", 300.0},
                new Object[]{"Bills", 700.0}
        );

        when(repository.getCategoryWiseTotals()).thenReturn(mockData);

        Map<String, Double> result = service.getCategoryWiseTotals(user);

        assertEquals(2, result.size());
        assertEquals(300.0, result.get("Food"));
    }

    @Test
    void testGetMonthlyTrend() {
        List<Object[]> income = new ArrayList<>();
        income.add(new Object[]{1, 1000.0});

        List<Object[]> expense = new ArrayList<>();
        expense.add(new Object[]{1, 500.0});

        when(repository.getMonthlyTotals("INCOME")).thenReturn(income);
        when(repository.getMonthlyTotals("EXPENSE")).thenReturn(expense);

        Map<String, Object> result = service.getMonthlyTrend(user);

        Map<Integer, Double> incomeMap = (Map<Integer, Double>) result.get("income");
        Map<Integer, Double> expenseMap = (Map<Integer, Double>) result.get("expense");

        assertEquals(1000.0, incomeMap.get(1));
        assertEquals(500.0, expenseMap.get(1));
    }

    @Test
    void testGetRecentActivity() {
        FinancialRecord record = new FinancialRecord();
        record.setId(1L);
        record.setAmount(100.0);
        record.setType("INCOME");
        record.setCategory("Salary");
        record.setDate(LocalDate.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUser(user);

        when(repository.findAll()).thenReturn(List.of(record));

        List<RecordResponse> result = service.getRecentActivity(user);

        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).getCategory());
    }
}

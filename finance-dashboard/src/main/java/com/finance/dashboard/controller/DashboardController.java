package com.finance.dashboard.controller;

import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.RecordService;
import com.finance.dashboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<?> getDashboardSummary() {
        User currentUser = getCurrentUser();
        // Get ALL records (not filtered by user)
        List<RecordResponse> allRecords = recordService.getUserRecords(currentUser, null, null, null, null);

        double totalIncome = allRecords.stream()
                .filter(r -> "INCOME".equals(r.getType()))
                .mapToDouble(RecordResponse::getAmount)
                .sum();

        double totalExpense = allRecords.stream()
                .filter(r -> "EXPENSE".equals(r.getType()))
                .mapToDouble(RecordResponse::getAmount)
                .sum();

        Map<String, Double> categoryWise = allRecords.stream()
                .filter(r -> "EXPENSE".equals(r.getType()))
                .collect(Collectors.groupingBy(
                        RecordResponse::getCategory,
                        Collectors.summingDouble(RecordResponse::getAmount)
                ));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", totalIncome - totalExpense);
        summary.put("categoryWise", categoryWise);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<?> getRecentActivity() {
        User currentUser = getCurrentUser();
        // Get ALL records (not filtered by user)
        List<RecordResponse> allRecords = recordService.getUserRecords(currentUser, null, null, null, null);

        List<RecordResponse> recentRecords = allRecords.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recentRecords);
    }
}
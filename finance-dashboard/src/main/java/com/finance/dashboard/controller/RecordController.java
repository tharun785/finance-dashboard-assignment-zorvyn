package com.finance.dashboard.controller;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.RecordService;
import com.finance.dashboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    // ALL ROLES can view records
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<?> getUserRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        User currentUser = getCurrentUser();
        List<RecordResponse> records = recordService.getUserRecords(currentUser, type, category, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("count", records.size());
        response.put("totalIncome", records.stream()
                .filter(r -> "INCOME".equals(r.getType()))
                .mapToDouble(RecordResponse::getAmount)
                .sum());
        response.put("totalExpense", records.stream()
                .filter(r -> "EXPENSE".equals(r.getType()))
                .mapToDouble(RecordResponse::getAmount)
                .sum());
        response.put("filters", Map.of(
                "type", type != null ? type : "",
                "category", category != null ? category : "",
                "startDate", startDate != null ? startDate.toString() : "",
                "endDate", endDate != null ? endDate.toString() : ""
        ));

        return ResponseEntity.status(201).body(response);
    }

    // ONLY ADMIN can create records
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRecord(@Valid @RequestBody RecordRequest recordRequest) {
        try {
            User currentUser = getCurrentUser();
            FinancialRecord record = recordService.createRecord(recordRequest, currentUser);
            RecordResponse response = recordService.convertToResponse(record);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ONLY ADMIN can update records
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRecord(@PathVariable Long id, @Valid @RequestBody RecordRequest recordRequest) {
        try {
            User currentUser = getCurrentUser();
            RecordResponse updatedRecord = recordService.updateRecord(id, recordRequest, currentUser);
            return ResponseEntity.ok(updatedRecord);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    // ONLY ADMIN can delete records
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRecord(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            recordService.deleteRecord(id, currentUser);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Record deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    // ALL ROLES can view a single record
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<?> getRecordById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            RecordResponse record = recordService.getRecordById(id, currentUser);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
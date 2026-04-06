package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordService {

    @Autowired
    private FinancialRecordRepository recordRepository;

    // Create record - Associate with the logged-in user
    public FinancialRecord createRecord(RecordRequest request, User user) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType().toUpperCase());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        // IMPORTANT: Set the user to satisfy NOT NULL constraint
        record.setUser(user); // This will store user_id in the database

        return recordRepository.save(record);
    }

    // Get ALL records (not filtered by user - all users see all records)
    public List<RecordResponse> getUserRecords(User user, String type, String category, LocalDate startDate, LocalDate endDate) {
        List<FinancialRecord> records;

        // Get ALL records regardless of which user created them
        if (startDate != null && endDate != null) {
            records = recordRepository.findByDateBetween(startDate, endDate);
        } else if (type != null && !type.isEmpty()) {
            records = recordRepository.findByType(type.toUpperCase());
        } else if (category != null && !category.isEmpty()) {
            records = recordRepository.findByCategory(category);
        } else {
            records = recordRepository.findAll(); // This gets ALL records
        }

        return records.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public RecordResponse updateRecord(Long id, RecordRequest request, User user) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        record.setAmount(request.getAmount());
        record.setType(request.getType().toUpperCase());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        return convertToResponse(recordRepository.save(record));
    }

    public void deleteRecord(Long id, User user) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        recordRepository.delete(record);
    }

    public RecordResponse convertToResponse(FinancialRecord record) {
        RecordResponse response = new RecordResponse();
        response.setId(record.getId());
        response.setAmount(record.getAmount());
        response.setType(record.getType());
        response.setCategory(record.getCategory());
        response.setDate(record.getDate());
        response.setDescription(record.getDescription());
        response.setCreatedAt(record.getCreatedAt());
        response.setCreatedBy(record.getUser() != null ? record.getUser().getUsername() : "System");
        return response;
    }

    public RecordResponse getRecordById(Long id, User user) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));
        return convertToResponse(record);
    }
}
package com.finance.dashboard.Service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;

import com.finance.dashboard.service.RecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private FinancialRecordRepository repository;

    @InjectMocks
    private RecordService service;

    private User user;
    private FinancialRecord record;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("admin");

        record = new FinancialRecord();
        record.setId(1L);
        record.setAmount(1000.0);
        record.setType("INCOME");
        record.setCategory("Salary");
        record.setDate(LocalDate.now());
        record.setUser(user);
    }

    @Test
    void testCreateRecord() {
        RecordRequest request = new RecordRequest();
        request.setAmount(1000.0);
        request.setType("income");

        when(repository.save(any())).thenReturn(record);

        FinancialRecord result = service.createRecord(request, user);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("INCOME");
    }

    @Test
    void testGetRecords() {
        when(repository.findAll()).thenReturn(List.of(record));

        List<RecordResponse> result =
                service.getUserRecords(user, null, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void testUpdateRecord() {
        when(repository.findById(1L)).thenReturn(Optional.of(record));
        when(repository.save(any())).thenReturn(record);

        RecordRequest req = new RecordRequest();
        req.setAmount(2000.0);
        req.setType("expense");

        RecordResponse res = service.updateRecord(1L, req, user);

        assertThat(res.getType()).isEqualTo("EXPENSE");
    }

    @Test
    void testDeleteRecord() {
        when(repository.findById(1L)).thenReturn(Optional.of(record));

        service.deleteRecord(1L, user);

        verify(repository).delete(record);
    }

    @Test
    void testGetRecordById() {
        when(repository.findById(1L)).thenReturn(Optional.of(record));

        RecordResponse res = service.getRecordById(1L, user);

        assertThat(res.getId()).isEqualTo(1L);
    }

    @Test
    void testGetRecord_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getRecordById(1L, user))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testConvertToResponse() {
        RecordResponse res = service.convertToResponse(record);

        assertThat(res.getCreatedBy()).isEqualTo("admin");
    }
}
package com.finance.dashboard.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.config.JwtAuthFilter;
import com.finance.dashboard.config.JwtService;
import com.finance.dashboard.controller.RecordController;
import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.User;
import com.finance.dashboard.service.RecordService;
import com.finance.dashboard.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @MockitoBean
    private RecordService recordService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private User testUser;
    private RecordRequest recordRequest;
    private RecordResponse recordResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");

        Mockito.when(userService.getUserByUsername("admin")).thenReturn(testUser);

        recordRequest = new RecordRequest();
        recordRequest.setAmount(1000.0);
        recordRequest.setType("EXPENSE");
        recordRequest.setCategory("Food");
        recordRequest.setDate(LocalDate.now());
        recordRequest.setDescription("Test");

        recordResponse = new RecordResponse();
        recordResponse.setId(1L);
        recordResponse.setAmount(1000.0);
        recordResponse.setType("EXPENSE");
        recordResponse.setCategory("Food");
        recordResponse.setDate(LocalDate.now());
        recordResponse.setCreatedAt(LocalDateTime.now());
        recordResponse.setCreatedBy("admin");
    }

    // ================= CREATE =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateRecord_Success() throws Exception {
        Mockito.when(recordService.createRecord(any(), any()))
                .thenReturn(null); // not used directly

        Mockito.when(recordService.convertToResponse(any()))
                .thenReturn(recordResponse);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(1000.0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateRecord_ValidationError() throws Exception {
        String invalid = "{}";

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    // ================= GET ALL =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllRecords() throws Exception {

        Mockito.when(recordService.getUserRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(recordResponse));

        mockMvc.perform(get("/api/records"))
                .andExpect(status().isCreated()) // your controller returns 201
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.records[0].id").value(1L));
    }

    // ================= GET BY ID =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetRecordById() throws Exception {

        Mockito.when(recordService.getRecordById(eq(1L), any()))
                .thenReturn(recordResponse);

        mockMvc.perform(get("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetRecord_NotFound() throws Exception {

        Mockito.when(recordService.getRecordById(eq(99L), any()))
                .thenThrow(new RuntimeException("Record not found"));

        mockMvc.perform(get("/api/records/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ================= UPDATE =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateRecord() throws Exception {

        Mockito.when(recordService.updateRecord(eq(1L), any(RecordRequest.class), any()))
                .thenReturn(recordResponse);

        mockMvc.perform(put("/api/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateRecord_Error() throws Exception {

        Mockito.when(recordService.updateRecord(eq(1L), any(RecordRequest.class), any()))
                .thenThrow(new RuntimeException("Forbidden"));

        mockMvc.perform(put("/api/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordRequest)))
                .andExpect(status().isForbidden())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // ================= DELETE =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteRecord() throws Exception {

        Mockito.doNothing().when(recordService).deleteRecord(eq(1L), any());

        mockMvc.perform(delete("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Record deleted successfully"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteRecord_Error() throws Exception {

        Mockito.doThrow(new RuntimeException("Forbidden"))
                .when(recordService).deleteRecord(eq(1L), any());

        mockMvc.perform(delete("/api/records/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // ================= FILTERS =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFilterByType() throws Exception {

        Mockito.when(recordService.getUserRecords(any(), eq("INCOME"), any(), any(), any()))
                .thenReturn(List.of(recordResponse));

        mockMvc.perform(get("/api/records").param("type", "INCOME"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filters.type").value("INCOME"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFilterByCategory() throws Exception {

        Mockito.when(recordService.getUserRecords(any(), any(), eq("Food"), any(), any()))
                .thenReturn(List.of(recordResponse));

        mockMvc.perform(get("/api/records").param("category", "Food"))
                .andExpect(jsonPath("$.filters.category").value("Food"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFilterByDateRange() throws Exception {

        String start = LocalDate.now().minusDays(5).toString();
        String end = LocalDate.now().toString();

        Mockito.when(recordService.getUserRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(recordResponse));

        mockMvc.perform(get("/api/records")
                        .param("startDate", start)
                        .param("endDate", end))
                .andExpect(jsonPath("$.filters.startDate").value(start))
                .andExpect(jsonPath("$.filters.endDate").value(end));
    }

    // ================= TOTAL CALC =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testTotalsCalculation() throws Exception {

        RecordResponse r1 = new RecordResponse();
        r1.setType("INCOME");
        r1.setAmount(5000.0);

        RecordResponse r2 = new RecordResponse();
        r2.setType("EXPENSE");
        r2.setAmount(2000.0);

        Mockito.when(recordService.getUserRecords(any(), any(), any(), any(), any()))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/records"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalIncome").value(5000.0))
                .andExpect(jsonPath("$.totalExpense").value(2000.0));
    }
}
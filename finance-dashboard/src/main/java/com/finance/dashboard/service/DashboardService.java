package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private FinancialRecordRepository recordRepository;

    // Get dashboard summary with GLOBAL data (not user-specific)
    public Map<String, Object> getDashboardSummary(User user) {
        Map<String, Object> summary = new HashMap<>();

        // Use global methods (not user-specific)
        Double totalIncome = recordRepository.sumByType("INCOME");
        Double totalExpense = recordRepository.sumByType("EXPENSE");

        summary.put("totalIncome", totalIncome != null ? totalIncome : 0.0);
        summary.put("totalExpense", totalExpense != null ? totalExpense : 0.0);
        summary.put("netBalance", (totalIncome != null ? totalIncome : 0.0) - (totalExpense != null ? totalExpense : 0.0));

        // Get global category totals
        List<Object[]> categoryTotals = recordRepository.getCategoryWiseTotals();
        Map<String, Double> categoryWise = new HashMap<>();
        if (categoryTotals != null) {
            for (Object[] row : categoryTotals) {
                if (row[0] != null && row[1] != null) {
                    categoryWise.put((String) row[0], (Double) row[1]);
                }
            }
        }
        summary.put("categoryWise", categoryWise);

        return summary;
    }

    public Double getTotalIncome(User user) {
        return recordRepository.sumByType("INCOME");
    }

    public Double getTotalExpense(User user) {
        return recordRepository.sumByType("EXPENSE");
    }

    public Double getNetBalance(User user) {
        Double income = getTotalIncome(user);
        Double expense = getTotalExpense(user);
        return (income != null ? income : 0.0) - (expense != null ? expense : 0.0);
    }

    public Map<String, Double> getCategoryWiseTotals(User user) {
        List<Object[]> results = recordRepository.getCategoryWiseTotals();
        Map<String, Double> categoryWise = new HashMap<>();
        if (results != null) {
            for (Object[] row : results) {
                if (row[0] != null && row[1] != null) {
                    categoryWise.put((String) row[0], (Double) row[1]);
                }
            }
        }
        return categoryWise;
    }

    public Map<String, Object> getMonthlyTrend(User user) {
        Map<String, Object> trend = new HashMap<>();
        List<Object[]> incomeMonthly = recordRepository.getMonthlyTotals("INCOME");
        List<Object[]> expenseMonthly = recordRepository.getMonthlyTotals("EXPENSE");

        Map<Integer, Double> incomeMap = new HashMap<>();
        Map<Integer, Double> expenseMap = new HashMap<>();

        if (incomeMonthly != null) {
            for (Object[] row : incomeMonthly) {
                if (row[0] != null && row[1] != null) {
                    incomeMap.put(((Number) row[0]).intValue(), (Double) row[1]);
                }
            }
        }

        if (expenseMonthly != null) {
            for (Object[] row : expenseMonthly) {
                if (row[0] != null && row[1] != null) {
                    expenseMap.put(((Number) row[0]).intValue(), (Double) row[1]);
                }
            }
        }

        trend.put("income", incomeMap);
        trend.put("expense", expenseMap);
        return trend;
    }

    public List<RecordResponse> getRecentActivity(User user) {
        // Get global recent records (not user-specific)
        List<FinancialRecord> recentRecords = recordRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());

        return recentRecords.stream()
                .map(record -> convertToResponse(record))
                .collect(Collectors.toList());
    }

    private RecordResponse convertToResponse(FinancialRecord record) {
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
}
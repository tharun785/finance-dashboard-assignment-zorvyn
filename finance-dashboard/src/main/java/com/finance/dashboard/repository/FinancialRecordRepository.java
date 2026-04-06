package com.finance.dashboard.repository;

import com.finance.dashboard.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Global queries (get all records regardless of user)
    List<FinancialRecord> findByType(String type);
    List<FinancialRecord> findByCategory(String category);
    List<FinancialRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Global sum queries
    @Query("SELECT SUM(r.amount) FROM FinancialRecord r WHERE r.type = :type")
    Double sumByType(@Param("type") String type);

    // Global category totals
    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r WHERE r.type = 'EXPENSE' GROUP BY r.category")
    List<Object[]> getCategoryWiseTotals();

    // Global monthly totals
    @Query("SELECT FUNCTION('MONTH', r.date), SUM(r.amount) FROM FinancialRecord r WHERE r.type = :type GROUP BY FUNCTION('MONTH', r.date)")
    List<Object[]> getMonthlyTotals(@Param("type") String type);

    // User-specific queries (keep for reference but may not be needed)
    List<FinancialRecord> findByUser(com.finance.dashboard.model.User user);
    List<FinancialRecord> findByUserAndType(com.finance.dashboard.model.User user, String type);
    List<FinancialRecord> findByUserAndCategory(com.finance.dashboard.model.User user, String category);
    List<FinancialRecord> findByUserAndDateBetween(com.finance.dashboard.model.User user, LocalDate startDate, LocalDate endDate);
}
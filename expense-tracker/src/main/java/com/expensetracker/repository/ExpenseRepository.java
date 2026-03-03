package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUser(User user, Pageable pageable);

    List<Expense> findByUserOrderByDateDesc(User user);

    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate")
    Page<Expense> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user")
    BigDecimal getTotalExpenseByUser(@Param("user") User user);

    @Query("SELECT e.category.name as category, SUM(e.amount) as total FROM Expense e WHERE e.user = :user GROUP BY e.category.name")
    List<Map<String, Object>> getCategoryWiseTotal(@Param("user") User user);

    @Query("SELECT strftime('%Y-%m', e.date) as month, SUM(e.amount) as total FROM Expense e WHERE e.user = :user GROUP BY month ORDER BY month DESC")
    List<Map<String, Object>> getMonthlyTrend(@Param("user") User user);
}

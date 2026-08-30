package com.tracker.expense.repository;

import com.tracker.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByUserIdOrderByExpenseDateDesc(UUID userId);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            UUID userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND e.type = :type " +
           "AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateRange(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}

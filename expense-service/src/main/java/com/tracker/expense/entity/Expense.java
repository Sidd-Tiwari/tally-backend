package com.tracker.expense.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_user_date", columnList = "userId, expenseDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private UUID categoryId;

    @Builder.Default
    @Column(nullable = false)
    private String type = "EXPENSE"; // EXPENSE or INCOME

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}

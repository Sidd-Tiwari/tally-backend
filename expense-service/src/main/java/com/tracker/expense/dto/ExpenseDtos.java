package com.tracker.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseDtos {

    public record CategoryRequest(
            @NotBlank String name,
            @NotBlank String type,
            String icon
    ) {}

    public record CategoryResponse(
            UUID id, String name, String type, String icon
    ) {}

    public record ExpenseRequest(
            @NotNull @Positive BigDecimal amount,
            @NotBlank String description,
            @NotNull UUID categoryId,
            @NotBlank String type,
            @NotNull LocalDate expenseDate
    ) {}

    public record ExpenseResponse(
            UUID id, BigDecimal amount, String description,
            UUID categoryId, String type, LocalDate expenseDate
    ) {}

    public record SummaryResponse(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal netBalance,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {}
}

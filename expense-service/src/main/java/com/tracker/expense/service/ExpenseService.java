package com.tracker.expense.service;

import com.tracker.expense.dto.ExpenseDtos.*;
import com.tracker.expense.entity.Expense;
import com.tracker.expense.exception.ApiExceptions;
import com.tracker.expense.repository.ExpenseRepository;
import com.tracker.expense.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public List<ExpenseResponse> getAll() {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(CurrentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = Expense.builder()
                .userId(CurrentUser.id())
                .amount(request.amount())
                .description(request.description())
                .categoryId(request.categoryId())
                .type(request.type())
                .expenseDate(request.expenseDate())
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense expense = getOwnedOrThrow(id);
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setCategoryId(request.categoryId());
        expense.setType(request.type());
        expense.setExpenseDate(request.expenseDate());
        return toResponse(expenseRepository.save(expense));
    }

    public void delete(UUID id) {
        expenseRepository.delete(getOwnedOrThrow(id));
    }

    /** Summary for a given month (defaults to current month if null). */
    public SummaryResponse getMonthlySummary(Integer year, Integer month) {
        YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        UUID userId = CurrentUser.id();
        BigDecimal income = expenseRepository.sumByUserAndTypeAndDateRange(userId, "INCOME", start, end);
        BigDecimal expense = expenseRepository.sumByUserAndTypeAndDateRange(userId, "EXPENSE", start, end);

        return new SummaryResponse(income, expense, income.subtract(expense), start, end);
    }

    private Expense getOwnedOrThrow(UUID id) {
        return expenseRepository.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Expense not found: " + id));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDescription(),
                e.getCategoryId(), e.getType(), e.getExpenseDate());
    }
}

package com.tracker.habit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class HabitDtos {

    public record HabitRequest(
            @NotBlank String name,
            @NotBlank String frequency
    ) {}

    public record HabitResponse(
            UUID id, String name, String frequency,
            Integer currentStreak, Integer longestStreak, LocalDate lastCompletedDate
    ) {}

    public record TaskRequest(
            @NotBlank String title,
            String description,
            LocalDate dueDate,
            @NotBlank String priority
    ) {}

    public record TaskResponse(
            UUID id, String title, String description,
            LocalDate dueDate, String priority, boolean completed
    ) {}
}
